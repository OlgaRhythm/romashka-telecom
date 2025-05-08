package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Call;
import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.repository.CallRepository;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.service.BillingService;
import com.romashka.romashka_telecom.brt.service.CdrDataFilter;
import com.romashka.romashka_telecom.brt.service.CdrDataProcessorService;
import com.romashka.romashka_telecom.common.config.TimeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * Пока работает только в модельном режиме
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CdrDataProcessorServiceImpl implements CdrDataProcessorService {

    private final TimeProperties timeProperties;
    private final TaskScheduler scheduler;
    private final BillingService billingService;
    private final CdrDataFilter filterService;
    private final CallerRepository callerRepo;
    private final CallRepository callRepo;

    /** последний «модельный» момент, который мы видели */
    private volatile LocalDateTime lastModelTime;
    /** за какой последний день уже списали */
    private volatile LocalDate lastBillingDate;
    /** handle на запланированный запуск (можно отменять) */
    private volatile ScheduledFuture<?> nextBillingFuture;
    private volatile boolean billingStarted = false;

    @Override
    public void process(List<CdrRecord> records) {
        // 1) Фильтрация абонентов оператора
        List<CdrRecord> filtered = filterService.filter(records);
        if (filtered.isEmpty()) {
            log.info("Нет записей CDR для обработки");
            return;
        }

        // 2) Собираем все уникальные callerNumber
        Set<String> callerNums = filtered.stream()
                .map(CdrRecord::getCallerNumber)
                .collect(Collectors.toSet());

        // 3) Один запрос в БД, чтобы получить Caller по номерам
        List<Caller> callers = callerRepo.findAllByNumberIn(new ArrayList<>(callerNums));
        Map<String, Caller> byNumber = callers.stream()
                .collect(Collectors.toMap(Caller::getNumber, c -> c));

        // 2–5) Мапим, сохраняем, логируем
        List<Call> callsToSave = mapAndCollectCalls(filtered);
        callRepo.saveAll(callsToSave);
        log.info("Сохранено {} звонков в таблицу calls", callsToSave.size());

        LocalDateTime maxModel = findMaxModelTime(filtered);

        // Если текущий день, то отправляем запросы по 1 на каждый звонок

        // Потом производим проверку тарифов
        if (!billingStarted) {
            // === первый файл ===
            initBilling(maxModel);
        } else {
            // === не первый файл ===
            updateBilling(maxModel);
        }

        // Снова отправляем запросы для тех, у кого звонок произошел в текущий день.
    }

    // ---------------------------------------------------
    // Шаги 2–4: собрать звонки в сущности Call
    // ---------------------------------------------------
    private List<Call> mapAndCollectCalls(List<CdrRecord> filtered) {
        Set<String> callerNums = filtered.stream()
                .map(CdrRecord::getCallerNumber).collect(Collectors.toSet());

        Map<String, Caller> callerByNumber = callerRepo
                .findAllByNumberIn(new ArrayList<>(callerNums))
                .stream()
                .collect(Collectors.toMap(Caller::getNumber, c -> c));

        return filtered.stream()
                .map(rec -> mapRecordToCall(rec, callerByNumber))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<Call> mapRecordToCall(CdrRecord rec, Map<String, Caller> byNumber) {
        Caller caller = byNumber.get(rec.getCallerNumber());
        if (caller == null) {
            log.warn("Не нашли в БД абонента с номером {}, пропускаем запись",
                    rec.getCallerNumber());
            return Optional.empty();
        }
        Call call = new Call();
        call.setCallerId(caller.getCallerId());
        call.setCallType(rec.getCallType());
        call.setContactNumber(rec.getContactNumber());
        call.setStartTime(rec.getStartTime());
        call.setEndTime(rec.getEndTime());
        return Optional.of(call);
    }

    // ---------------------------------------------------
    // Шаг 6.1: первичная инициализация биллинга
    // ---------------------------------------------------
    private void initBilling(LocalDateTime maxModel) {
        billingStarted  = true;
        lastModelTime   = maxModel;
        // последний день, за который уже не платили — день до maxModel
        lastBillingDate = maxModel.toLocalDate().minusDays(1);
        scheduleNextBilling();
    }

    // ---------------------------------------------------
    // Шаг 6.2: для последующих файлов
    // ---------------------------------------------------
    private void updateBilling(LocalDateTime maxModel) {
        // сглаживаем «назад» и «до 5 мин. вперёд»
        Duration jump = Duration.between(lastModelTime, maxModel);
        if (jump.isNegative() || jump.toMinutes() <= 5) {
            maxModel = lastModelTime;
        }

        // отрабатываем пропущенные дни
        LocalDate target = maxModel.toLocalDate();
        for (LocalDate day = lastBillingDate.plusDays(1);
             !day.isAfter(target);
             day = day.plusDays(1)) {

            billingService.chargeMonthlyFee(day);
            lastBillingDate = day;
        }

        lastModelTime = maxModel;

        // если догнали запланированный запуск — перезапланируем
        if (nextBillingFuture != null && nextBillingFuture.cancel(false)) {
            scheduleNextBilling();
        }
    }

    // ---------------------------------------------------
    // Помощник: найти максимальное endTime
    // ---------------------------------------------------
    private LocalDateTime findMaxModelTime(List<CdrRecord> records) {
        return records.stream()
                .map(CdrRecord::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElseThrow();
    }

    /**
     * Вычисляем реальный Instant для следующей модельной полуночи
     * и ставим одноразовый запуск.
     */
    private void scheduleNextBilling() {
        // когда в модельном времени у нас будет следующий день в 00:00?
        LocalDateTime nextModelMidnight = lastBillingDate.plusDays(1).atStartOfDay();
        // сколько в милисекундах модельного времени до него?
        Duration modelDelta = Duration.between(lastModelTime, nextModelMidnight);
        if (modelDelta.isNegative() || modelDelta.isZero()) {
            // если уже «прошла» — запускаем немедленно
            billingService.chargeMonthlyFee(nextModelMidnight.toLocalDate());
            lastBillingDate = nextModelMidnight.toLocalDate();
            lastModelTime   = nextModelMidnight;
            // и рекурсивно запланируем дальше
            scheduleNextBilling();
            return;
        }
        // переводим модельный интервал в реальный
        long realDelay = (long)(modelDelta.toMillis() / timeProperties.getCoefficient());
        Instant runAt = Instant.now().plusMillis(realDelay);

        try {
            nextBillingFuture = scheduler.schedule(
                    () -> {
                        billingService.chargeMonthlyFee(nextModelMidnight.toLocalDate());
                        lastBillingDate = nextModelMidnight.toLocalDate();
                        lastModelTime = nextModelMidnight;
                        scheduleNextBilling();  // рекурсивно на следующий день
                    },
                    runAt
            );
        }
        catch (RejectedExecutionException ex) {
            log.warn("Scheduler is shutting down, skipping next billing task", ex);
        }
    }
}

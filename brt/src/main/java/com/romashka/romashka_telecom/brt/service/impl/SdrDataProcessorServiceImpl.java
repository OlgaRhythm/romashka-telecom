package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Call;
import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.repository.CallRepository;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.service.BillingService;
import com.romashka.romashka_telecom.brt.service.SdrDataFilter;
import com.romashka.romashka_telecom.brt.service.SdrDataProcessorService;
import com.romashka.romashka_telecom.common.config.TimeProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SdrDataProcessorServiceImpl implements SdrDataProcessorService {

    private final TimeProperties timeProperties;
    private final TaskScheduler scheduler;
    private final BillingService billingService;

    private final SdrDataFilter filterService;
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
        // Фильтрация абонентов оператора
        List<CdrRecord> filtered = filterService.filter(records);
        if (filtered.isEmpty()) {
            log.info("Нет записей CDR для обработки");
            return;
        }
        // 2) Собираем все уникальные callerNumber
        Set<String> callerNums = filtered.stream()
                .map(CdrRecord::getCallerNumber)
                .collect(Collectors.toSet());

        // 3) Один запрос в БД, чтобы получить Caller-ы по номерам
        List<Caller> callers = callerRepo.findAllByNumberIn(new ArrayList<>(callerNums));
        Map<String, Caller> byNumber = callers.stream()
                .collect(Collectors.toMap(Caller::getNumber, c -> c));

        // 4) Смапим CdrRecord → Call
        List<Call> callsToSave = new ArrayList<>(filtered.size());
        for (CdrRecord rec : filtered) {
            Caller caller = byNumber.get(rec.getCallerNumber());
            if (caller == null) {
                log.warn("Не нашли в БД абонента с номером {}, пропускаем запись", rec.getCallerNumber());
                continue;
            }
            Call call = new Call();
            call.setCallerId(caller.getCallerId());
            call.setCallType(rec.getCallType());
            call.setContactNumber(rec.getContactNumber());
            call.setStartTime(rec.getStartTime());
            call.setEndTime(rec.getEndTime());
            callsToSave.add(call);
        }

        // 5) Сохраняем пачкой
        callRepo.saveAll(callsToSave);
        log.info("Сохранено {} звонков в таблицу calls", callsToSave.size());

        // TODO: синхронизация модельного времени
        LocalDateTime maxModel = filtered.stream()
                .map(CdrRecord::getEndTime)
                .max(Comparator.naturalOrder())
                .orElseThrow();

        if (!billingStarted) {
            // === первый файл ===
            billingStarted   = true;
            lastModelTime    = maxModel;
            // последний день, за который уже не платили — тот, что до даты maxModel
            lastBillingDate  = maxModel.toLocalDate().minusDays(1);
            // запускаем первый раз, «догнав» эту модельную точку
            scheduleNextBilling();
        } else {
            // === не первый файл ===
            // 3) сглаживаем «назад» и «до 5 мин. вперёд»
            Duration jump = Duration.between(lastModelTime, maxModel);
            if (jump.isNegative() || jump.toMinutes() <= 5) {
                maxModel = lastModelTime;
            }

            // 4) отрабатываем пропущенные дни
            for (LocalDate d = lastBillingDate.plusDays(1);
                 !d.isAfter(maxModel.toLocalDate());
                 d = d.plusDays(1)) {
                billingService.chargeMonthlyFee(d);
                lastBillingDate = d;
            }

            lastModelTime = maxModel;

            // 5) если этот CDR «догнал» запланированный запуск раньше — выполняем догоняющего биллинга
            if (nextBillingFuture != null && nextBillingFuture.cancel(false)) {
                scheduleNextBilling(); // пересчитаем «следующую» модельную полуночь
            }
        }
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

        nextBillingFuture = scheduler.schedule(
                () -> {
                    billingService.chargeMonthlyFee(nextModelMidnight.toLocalDate());
                    lastBillingDate = nextModelMidnight.toLocalDate();
                    lastModelTime   = nextModelMidnight;
                    scheduleNextBilling();  // рекурсивно на следующий день
                },
                runAt
        );
    }
}

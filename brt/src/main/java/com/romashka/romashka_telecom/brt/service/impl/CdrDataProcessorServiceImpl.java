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
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.time.format.DateTimeFormatter;

/**
 * Пока работает только в модельном режиме
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CdrDataProcessorServiceImpl implements CdrDataProcessorService {

    private final Integer DELAY_HOURS = 12;

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

    private final Map<LocalDate, List<CdrRecord>> callsByDate = new ConcurrentHashMap<>();
    private final Set<LocalDate> processedDates = ConcurrentHashMap.newKeySet();
    @Transactional
    @Override
    public void process(List<CdrRecord> records) {
        // 1) Фильтрация абонентов оператора
        List<CdrRecord> filtered = filterService.filter(records);
        if (filtered.isEmpty()) {
            log.info("Нет записей CDR для обработки");
            return;
        }

        // 2–5) Мапим, сохраняем, логируем
        List<Call> callsToSave = mapAndCollectCalls(filtered);
        callRepo.saveAll(callsToSave);
        log.info("Сохранено {} звонков в таблицу calls", callsToSave.size());

        // Группируем записи по дате окончания звонка
        filtered.forEach(record -> {
            LocalDate date = record.getEndTime().toLocalDate();
            log.debug("Добавление записи за дату: {}", date);
            callsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(record);
        });
       
        // 6) Обновляем modelTime
        LocalDateTime maxModel = filtered.stream()
                .map(CdrRecord::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(lastModelTime != null ? lastModelTime : LocalDateTime.now());
        lastModelTime = maxModel;

        // Потом производим проверку тарифов
        if (!billingStarted) {
            // === первый файл ===
            initBilling(maxModel);
        } else {
            // === не первый файл ===
            updateBilling(maxModel);
        }
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

        // Находим минимальную дату среди всех записей
        LocalDate minDate = callsByDate.keySet().stream()
                .min(LocalDate::compareTo)
                .orElse(maxModel.toLocalDate());

        // последний день, за который уже не платили — день до maxModel
        lastBillingDate = minDate.minusDays(1); // Устанавливаем на день перед первым днём данных
        scheduleNextBilling();
    }

    // ---------------------------------------------------
    // Шаг 6.2: для последующих файлов
    // ---------------------------------------------------
    private void updateBilling(LocalDateTime maxModel) {
        // сглаживаем «назад» и «до 5 мин. вперёд»
//        Duration jump = Duration.between(lastModelTime, maxModel);
//        if (jump.isNegative() || jump.toMinutes() <= 5) {
//            maxModel = lastModelTime;
//        } // Убрано, чтобы не пропускать дни

//        // отрабатываем пропущенные дни
//        LocalDate target = maxModel.toLocalDate();
//
//        for (LocalDate day = lastBillingDate.plusDays(1);
//             !day.isAfter(target);
//             day = day.plusDays(1)) {
//            // Проверяем и обрабатываем звонки за день
//            if (hasUnprocessedCalls(day)) {
//                processCallsForDate(day);
//            }
//            // Тарифицируем абонента за день
//            billingService.chargeMonthlyFee(day);
//
//            // Обновляем lastBillingDate сразу после обработки
//            lastBillingDate = day;
//        }
//
//        lastModelTime = maxModel;
//
//        // если догнали запланированный запуск — перезапланируем
//        if (nextBillingFuture != null && nextBillingFuture.cancel(false)) {
//            scheduleNextBilling();
//        }

        LocalDate target = maxModel.toLocalDate();

        for (LocalDate day = lastBillingDate.plusDays(1);
             !day.isAfter(target);
             day = day.plusDays(1)) {

            // Всегда обновляем lastBillingDate, даже если день пустой
            lastBillingDate = day;

            if (hasUnprocessedCalls(day)) {
                processCallsForDate(day);
                // Перенести вызов chargeMonthlyFee внутрь условия
                billingService.chargeMonthlyFee(day);
            }
        }
        lastModelTime = maxModel;
    }


    /**
     * Вычисляем реальный Instant для следующей модельной полуночи
     * и ставим одноразовый запуск.
     */
    private void scheduleNextBilling() {
//        // когда в модельном времени у нас будет следующий день в 00:00?
//        LocalDateTime nextModelBillingTime  = lastBillingDate.plusDays(1)
//                                                         .atStartOfDay()
//                                                         .plusHours(DELAY_HOURS);
//        // сколько в милисекундах модельного времени до списания?
//        Duration modelDelta = Duration.between(lastModelTime, nextModelBillingTime);
//        if (modelDelta.isNegative() || modelDelta.isZero()) {
//            // если уже «прошла» — запускаем немедленно
//            executeBilling(nextModelBillingTime.toLocalDate());
//            return;
//        }
//        // переводим модельный интервал в реальный
//        long realDelay = (long)(modelDelta.toMillis() / timeProperties.getCoefficient());
//        Instant runAt = Instant.now().plusMillis(realDelay);
//
//        try {
//            nextBillingFuture = scheduler.schedule(
//                    () -> {
//                        executeBilling(nextModelBillingTime.toLocalDate());
//                        scheduleNextBilling();  // рекурсивно на следующий день
//                    },
//                    runAt
//            );
//        }
//        catch (RejectedExecutionException ex) {
//            log.warn("Scheduler is shutting down, skipping next billing task", ex);
//        }
    }

    private void executeBilling(LocalDate billingDate) {
//        LocalDate targetDate = billingDate.minusDays(1);
//        log.debug("Проверка billingDate={}, targetDate={}", billingDate, targetDate);
//
//        if (hasUnprocessedCalls(targetDate)) {
//            log.warn("Есть необработанные звонки за {}", targetDate);
//            processCallsForDate(targetDate); // Обработать звонки
//        }


        for (LocalDate day = lastBillingDate.plusDays(1); !day.isAfter(billingDate); day = day.plusDays(1)) {
            log.debug("Проверка billingDate={}, targetDate={}", billingDate, day);
            if (hasUnprocessedCalls(day)) {
                processCallsForDate(day);
            }
            billingService.chargeMonthlyFee(day);
            lastBillingDate = day;
        }

//        billingService.chargeMonthlyFee(billingDate);
//        lastBillingDate = billingDate;
//        processedDates.add(targetDate);
        processedDates.add(billingDate);
        lastModelTime = billingDate.atStartOfDay().plusHours(DELAY_HOURS); // Обновляем время
    }

    private void processCallsForDate(LocalDate date) {
        log.info("🟢 Начата обработка звонков за {}", date);

        List<CdrRecord> calls = callsByDate.getOrDefault(date, Collections.emptyList());
        if (calls.isEmpty()) return;

        log.info("📊 Всего звонков для обработки за {}: {}", date, calls.size());
    
        // Логика обработки звонков (например, расчет стоимости)
        calls.forEach(call -> {
            log.info("📞 Обработка звонка: "
                + "[Caller: {}, Contact: {}, Type: {}, Start: {}, End: {}, Duration: {}]",
            call.getCallerNumber(),
            call.getContactNumber(),
            call.getCallType(),
            call.getStartTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
            call.getEndTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
            Duration.between(call.getStartTime(), call.getEndTime()).toMinutes() + " мин"
        );
            // ... ваша бизнес-логика ...
        });
    
        processedDates.add(date); // Пометить как обработанные
        log.info("🔵 Успешно обработано звонков за {}: {}", date, calls.size());
    log.debug("📌 Список обработанных звонков за {}:\n{}", 
        date, 
        calls.stream()
            .map(c -> "▸ " + c.getCallerNumber() + " → " + c.getContactNumber())
            .collect(Collectors.joining("\n"))
    );
    }

    private boolean hasUnprocessedCalls(LocalDate date) {
        return callsByDate.containsKey(date) && !processedDates.contains(date);
    }
}

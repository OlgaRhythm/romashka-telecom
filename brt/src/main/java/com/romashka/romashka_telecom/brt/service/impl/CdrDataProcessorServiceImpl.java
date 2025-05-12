package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Call;
import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.BillingMessage;
import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.enums.NetworkType;
import com.romashka.romashka_telecom.brt.repository.CallRepository;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.repository.CallerResourceRepository;
import com.romashka.romashka_telecom.brt.service.BillingService;
import com.romashka.romashka_telecom.brt.service.CdrDataFilter;
import com.romashka.romashka_telecom.brt.service.CdrDataProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса для обработки CDR-записей.
 * Сервис отвечает за:
 * 1. Фильтрацию записей по абонентам оператора
 * 2. Сохранение информации о звонках в базу данных
 * 3. Группировку звонков по датам
 * 4. Инициализацию и обновление модельного времени
 * 5. Обработку звонков для биллинга
 * 6. Управление списанием абонентской платы
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CdrDataProcessorServiceImpl implements CdrDataProcessorService {

    private final Integer DELAY_HOURS = 12;

    private final BillingService billingService;
    private final CdrDataFilter filterService;
    private final CallerRepository callerRepo;
    private final CallRepository callRepo;
    private final CallerResourceRepository callerResourceRepository;

    /** последний «модельный» момент, который мы видели */
    private volatile LocalDateTime lastModelTime;
    /** за какой последний день уже списали */
    private volatile LocalDate lastBillingDate;
    private volatile boolean billingStarted = false;

    private final Map<LocalDate, List<CdrRecord>> callsByDate = new ConcurrentHashMap<>();
    private final Set<LocalDate> processedDates = ConcurrentHashMap.newKeySet();

    /**
     * Обрабатывает список CDR-записей.
     * Выполняет следующие действия:
     * 1. Фильтрует записи по абонентам оператора
     * 2. Сохраняет информацию о звонках в базу данных
     * 3. Группирует записи по датам
     * 4. Обновляет модельное время
     * 5. Инициализирует или обновляет биллинг
     *
     * @param records список CDR-записей для обработки
     */
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

    /**
     * Преобразует список CDR-записей в список сущностей Call.
     * Для каждой записи находит соответствующего абонента в базе данных
     * и создает объект Call с информацией о звонке.
     *
     * @param filtered отфильтрованный список CDR-записей
     * @return список сущностей Call для сохранения
     */
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

    /**
     * Преобразует одну CDR-запись в сущность Call.
     * Находит абонента по номеру телефона и создает объект Call
     * с информацией о звонке.
     *
     * @param rec CDR-запись для преобразования
     * @param byNumber карта абонентов по номерам телефонов
     * @return Optional с объектом Call, если абонент найден
     */
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

    /**
     * Инициализирует биллинг при получении первого файла CDR.
     * Устанавливает начальные значения для модельного времени
     * и даты последнего списания.
     *
     * @param maxModel максимальное модельное время из записей
     */
    private void initBilling(LocalDateTime maxModel) {
        billingStarted  = true;
        lastModelTime   = maxModel;

        // Находим минимальную дату среди всех записей
        LocalDate minDate = callsByDate.keySet().stream()
                .min(LocalDate::compareTo)
                .orElse(maxModel.toLocalDate());

        // последний день, за который уже не платили — день до maxModel
        lastBillingDate = minDate.minusDays(1); // Устанавливаем на день перед первым днём данных
    }

    /**
     * Обновляет биллинг при получении последующих файлов CDR.
     * Обрабатывает звонки за каждый день с момента последнего списания
     * до текущей даты и инициирует списание абонентской платы.
     *
     * @param maxModel максимальное модельное время из записей
     */
    private void updateBilling(LocalDateTime maxModel) {
        // Берется на день меньше
        LocalDate target = maxModel.toLocalDate().minusDays(1);

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
     * Обрабатывает звонки за указанную дату.
     * Для каждого звонка:
     * 1. Рассчитывает длительность
     * 2. Определяет тип сети (внутренняя/внешняя)
     * 3. Собирает информацию о ресурсах абонента
     * 4. Формирует и отправляет сообщение для биллинга
     *
     * @param date дата для обработки звонков
     */
    private void processCallsForDate(LocalDate date) {
        log.info("🟢 Начата обработка звонков за {}", date);

        List<CdrRecord> calls = callsByDate.getOrDefault(date, Collections.emptyList());
        if (calls.isEmpty()) return;

        log.info("📊 Всего звонков для обработки за {}: {}", date, calls.size());

        // Логика обработки звонков (например, расчет стоимости)
        calls.forEach(call -> {
            long durationMinutes = Duration.between(call.getStartTime(), call.getEndTime()).toMinutes();

            // Получаем callerId из базы данных по номеру
            Caller caller = callerRepo.findByNumber(call.getCallerNumber())
                .orElseThrow(() -> new RuntimeException("Caller not found: " + call.getCallerNumber()));

            // Проверяем, является ли контакт абонентом Ромашки
            boolean isInternalCall = callerRepo.findByNumber(call.getContactNumber()).isPresent();
            NetworkType networkType = isInternalCall ? NetworkType.INTERNAL : NetworkType.EXTERNAL;

            // 1) собираем сообщение
            Map<String, Double> resources = new HashMap<>();
            
            // Добавляем баланс абонента
            resources.put("money", caller.getBalance().doubleValue());
            
            // Добавляем остальные ресурсы
            callerResourceRepository.findByCallerId(caller.getCallerId())
                    .forEach(cr -> resources.put(
                            cr.getResourceId().getResourceName(),
                            cr.getCurrentBalance().doubleValue()
                    ));

            BillingMessage msg = BillingMessage.builder()
                    .callerId(caller.getCallerId())
                    .rateId(caller.getRateId())
                    .durationMinutes(durationMinutes)
                    .callType(call.getCallType())
                    .networkType(networkType)
                    .resources(resources)
                    .build();

            log.info("🚀 Сформировано сообщение для биллинга: {}", msg);

            // 2) отправляем
            billingService.processAndSendBillingData(msg);

            // и по-прежнему логируем
            log.info("📞 Сформирован и отправлен биллинг для звонка: {}", msg);
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

    /**
     * Проверяет, есть ли необработанные звонки за указанную дату.
     *
     * @param date дата для проверки
     * @return true, если есть необработанные звонки
     */
    private boolean hasUnprocessedCalls(LocalDate date) {
        return callsByDate.containsKey(date) && !processedDates.contains(date);
    }
}

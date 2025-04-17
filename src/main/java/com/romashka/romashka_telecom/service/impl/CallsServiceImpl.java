package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.Caller;
import com.romashka.romashka_telecom.entity.CdrData;
import com.romashka.romashka_telecom.enums.CallType;
import com.romashka.romashka_telecom.repository.CallerRepository;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CallsService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CallsServiceImpl implements CallsService {
    private static final Logger logger = LoggerFactory.getLogger(CallsServiceImpl.class);
    private final CallerRepository callerRepository;
    private final CdrDataRepository cdrDataRepository;
    private final Random random = new Random();

    // Абоненты -> до какого времени они заняты
    private final ConcurrentMap<String, LocalDateTime> subscriberBusyUntil = new ConcurrentHashMap<>();
    // Абоненты -> объект блокировки
    private final ConcurrentHashMap<String, ReentrantLock> subscriberLocks = new ConcurrentHashMap<>();

    // TODO: значение задается в application.yml
    @Value("${thread.pool.size:4}") // обычно берут количество ядер процессора
    private int threadPoolSize;
    private ExecutorService executor;

    @Autowired
    CallsServiceImpl(CallerRepository callerRepository, CdrDataRepository cdrDataRepository) {
        this.callerRepository = callerRepository;
        this.cdrDataRepository = cdrDataRepository;
    }

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Метод, запускающий генерацию звонков.
     */
    @Override
    public void generateCalls() {
        List<Caller> callers = callerRepository.findAll();
        if (callers.size() < 2) {
            throw new IllegalStateException("Недостаточно абонентов в базе данных");
        }

        // Инициализация структур данных для абонентов
        callers.forEach(caller -> {
            subscriberBusyUntil.putIfAbsent(caller.getCallerNumber(), LocalDateTime.MIN);
            subscriberLocks.putIfAbsent(caller.getCallerNumber(), new ReentrantLock());
        });

        // Увеличиваем количество генерируемых звонков
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                try {
                    generateAndSaveCallPair(callers);
                } catch (Exception e) {
                    logger.error("Ошибка при генерации звонка", e);
                }
            });
        }
    }

    /**
     * Генерирует один случайный звонок с учетом синхронизации по абонентам.
     */
    private void generateAndSaveCallPair(List<Caller> callers) {
        final int MAX_ATTEMPTS = 500; // Увеличиваем количество попыток
        int attempts = 0;

        while (attempts++ < MAX_ATTEMPTS) {
            Caller caller = getRandomCaller(callers);
            Caller contact = getRandomCaller(callers);

            if (caller.getCallerNumber().equals(contact.getCallerNumber())) {
                continue;
            }

            // Упрощаем блокировку - блокируем только на время проверки/обновления
            synchronized (this) {
                LocalDateTime start = generateRandomDateTime();
                long duration = 60 + random.nextInt(540);
                LocalDateTime end = start.plusSeconds(duration);

                // Проверяем только пересечение интервалов
                if (subscriberBusyUntil.get(caller.getCallerNumber()).isAfter(start) ||
                        subscriberBusyUntil.get(contact.getCallerNumber()).isAfter(start)) {
                    continue;
                }

                // Обновляем время занятости
                subscriberBusyUntil.put(caller.getCallerNumber(), end);
                subscriberBusyUntil.put(contact.getCallerNumber(), end);

                // Сохраняем записи
                saveCallRecords(caller, contact, start, end);
                return;
            }
        }
        logger.warn("Не удалось сгенерировать звонок после {} попыток", MAX_ATTEMPTS);
    }


    private Caller getRandomCaller(List<Caller> subscribers) {
        return subscribers.get(random.nextInt(subscribers.size()));
    }

    private LocalDateTime generateRandomDateTime() {
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        int randomDayOfYear = random.nextInt(startOfYear.lengthOfYear());
        LocalDate randomDate = startOfYear.plusDays(randomDayOfYear);

        LocalTime randomTime = LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));

        return LocalDateTime.of(randomDate, randomTime);
    }

    @Transactional
    protected void saveCallRecords(Caller caller, Caller contact, LocalDateTime start, LocalDateTime end) {
        // Сохраняем исходящий звонок
        saveSingleCall(caller, contact, CallType.OUTGOING, start, end);
        // Сохраняем входящий звонок
        saveSingleCall(contact, caller, CallType.INCOMING, start, end);
    }

    @Transactional
    protected void saveSingleCall(Caller caller, Caller contact, CallType callType,
                                  LocalDateTime start, LocalDateTime end) {
        if (start.toLocalDate().equals(end.toLocalDate())) {
            // Звонок в пределах одного дня
            saveCall(caller, contact, callType, start, end);
        } else {
            // Звонок через полночь - разбиваем на две части
            LocalDateTime midnight = LocalDateTime.of(start.toLocalDate(), LocalTime.MAX);
            saveCall(caller, contact, callType, start, midnight);
            saveCall(caller, contact, callType, midnight.plusSeconds(1), end);
        }
    }

    private void saveCall(Caller caller, Caller contact, CallType callType,
                          LocalDateTime start, LocalDateTime end) {
        CdrData call = new CdrData();
        call.setCallType(callType);
        call.setCallerNumber(caller.getCallerNumber());
        call.setContactNumber(contact.getCallerNumber());
        call.setStartTime(start);
        call.setEndTime(end);

        cdrDataRepository.save(call);
        logger.info("Сохранён звонок: {} [{}] -> {}, {} - {}",
                caller.getCallerNumber(), callType, contact.getCallerNumber(), start, end);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}

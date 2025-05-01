package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.common.config.TimeProperties;

import com.romashka.romashka_telecom.cdr.entity.Caller;
import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.enums.CallType;
import com.romashka.romashka_telecom.cdr.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.cdr.repository.CallerRepository;
import com.romashka.romashka_telecom.cdr.repository.CdrDataRepository;
import com.romashka.romashka_telecom.cdr.service.CallsGenerationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Сервис генерации звонков между абонентами.
 * Создаёт пары записей о звонках (входящий и исходящий) с учётом занятости абонентов и реалистичных временных интервалов.
 */
@Service
@Getter
@Setter
@RequiredArgsConstructor
public class CallsGenerationServiceImpl implements CallsGenerationService {

    private final TimeProperties timeProps;

    private final ApplicationEventPublisher eventPublisher;
    private final CallerRepository callerRepo;
    private final CdrDataRepository cdrRepo;
    private final Random rnd = new Random();

    @Value("${thread.pool.size:8}")
    private int threadPoolSize;

    /** Пара звонков даёт 2 записи => totalPairs*2 записей */
    @Value("${calls.total_pairs:3000}")
    private int totalPairs;

    private static final int MIN_CALL_DURATION = 60; // минимальная продолжительность звонка в секундах
    private static final int MAX_CALL_DURATION = 14 * 60; // максимальная продолжительность звонка в секундах

    /**
     * Основной метод генерации звонков.
     * Выполняет генерацию слотов, распределение задач, синхронизацию и сохранение в БД.
     */
    @Override
    public void generateCalls() {
        List<Caller> callers = getCallers();
        // 1) Генерируем и сортируем слоты времени
        List<LocalDateTime> slots = generateSortedTimeSlots();

        // 2) Подготовка синхронизации: листы интервалов
        ConcurrentMap<String, List<Interval>> busyIntervals = new ConcurrentHashMap<>();
        ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
        for (Caller c : callers) {
            busyIntervals.put(c.getCallerNumber(), new ArrayList<>());
            locks.put(c.getCallerNumber(), new ReentrantLock());
        }

        // 3) Пул для записи в БД
        ExecutorService exec = Executors.newFixedThreadPool(threadPoolSize);

        // 4) Каждый слот — в своё generateAt
        List<Future<?>> futures = submitCallGenerationTasks(slots, callers, busyIntervals, locks, exec);

        // 5) Ждём сохранения
        waitForCompletion(futures);

        exec.shutdown();

        // 6) Событие завершения
        eventPublisher.publishEvent(new CallsGenerationCompletedEvent(totalPairs * 2));
    }

    /**
     * Получает список всех абонентов.
     * @return список абонентов
     * @throws IllegalStateException если абонентов меньше двух
     */
    private List<Caller> getCallers() {
        List<Caller> callers = callerRepo.findAll();
        if (callers.size() < 2) {
            throw new IllegalStateException("Нужно ≥2 абонента");
        }
        return callers;
    }

    /**
     * Генерирует отсортированный список случайных временных слотов в пределах года.
     * @return отсортированный список временных слотов
     */
    private List<LocalDateTime> generateSortedTimeSlots() {
        LocalDateTime start = timeProps.getStart();
        Duration fullSpan = Duration.between(start, timeProps.getEnd());
        List<LocalDateTime> slots = new ArrayList<>(totalPairs);
        for (int i = 0; i < totalPairs; i++) {
            // равномерно-случайный сдвиг
            long randomMillis = (long)(rnd.nextDouble() * fullSpan.toMillis());
            slots.add(start.plus(randomMillis, ChronoUnit.MILLIS));
        }
        slots.sort(Comparator.naturalOrder());
        return slots;
    }

    /**
     * Отправляет задачи генерации звонков в пул потоков.
     * @param slots временные слоты
     * @param callers список абонентов
     * @param busyIntervals занятые интервалы
     * @param locks блокировки для синхронизации
     * @param exec пул потоков
     * @return список Future задач
     */
    private List<Future<?>> submitCallGenerationTasks(List<LocalDateTime> slots, List<Caller> callers,
                                                      ConcurrentMap<String, List<Interval>> busyIntervals,
                                                      ConcurrentMap<String, ReentrantLock> locks, ExecutorService exec) {
        List<Future<?>> futures = new ArrayList<>();
        for (LocalDateTime start : slots) {
            futures.add(exec.submit(() -> generateAt(start, callers, busyIntervals, locks)));
        }
        return futures;
    }

    /**
     * Ожидает завершения всех задач генерации звонков.
     * @param futures список Future задач
     */
    private void waitForCompletion(List<Future<?>> futures) {
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Генерирует одну пару звонков (входящий и исходящий) в заданный момент времени.
     * Проверяет, чтобы абоненты были свободны в этот период.
     */
    private void generateAt(
            LocalDateTime start,
            List<Caller> callers,
            ConcurrentMap<String, List<Interval>> busyIntervals,
            Map<String, ReentrantLock> locks
    ) {
        while (true) {
            Caller a = getRandomCaller(callers);
            Caller b = getRandomCallerExcluding(a, callers);;

            String an = a.getCallerNumber(), bn = b.getCallerNumber();
            String k1 = an.compareTo(bn) < 0 ? an : bn;
            String k2 = an.compareTo(bn) < 0 ? bn : an;
            ReentrantLock l1 = locks.get(k1), l2 = locks.get(k2);

            l1.lock(); l2.lock();
            try {
                long duration = MIN_CALL_DURATION + rnd.nextInt(MAX_CALL_DURATION - MIN_CALL_DURATION);
                LocalDateTime end = start.plusSeconds(duration);

                List<Interval> listA = busyIntervals.get(an);
                List<Interval> listB = busyIntervals.get(bn);

                // Проверка полного пересечения в двух списках
                if (!overlapsAny(listA, start, end) && !overlapsAny(listB, start, end)) {
                    // Бронируем оба интервала
                    listA.add(new Interval(start, end));
                    listB.add(new Interval(start, end));
                    // Сохраняем, разбивая через полночь
                    savePair(a, b, start, end);
                    return;
                }
            } finally {
                l2.unlock();
                l1.unlock();
            }
        }
    }

    /**
     * Возвращает случайного абонента.
     */
    private Caller getRandomCaller(List<Caller> callers) {
        return callers.get(rnd.nextInt(callers.size()));
    }

    /**
     * Возвращает случайного абонента, отличного от указанного.
     */
    private Caller getRandomCallerExcluding(Caller excluded, List<Caller> callers) {
        Caller b;
        do {
            b = callers.get(rnd.nextInt(callers.size()));
        } while (b.getCallerNumber().equals(excluded.getCallerNumber()));
        return b;
    }

    /**
     * Проверяет, пересекается ли указанный интервал с любым из списка.
     */
    private boolean overlapsAny(List<Interval> list, LocalDateTime s, LocalDateTime e) {
        return list.stream().anyMatch(iv -> s.isBefore(iv.end) && e.isAfter(iv.start));
    }

    /**
     * Сохраняет пару звонков (входящий и исходящий), разбивая при необходимости по дням.
     */
    public void savePair(Caller a, Caller b, LocalDateTime start, LocalDateTime end) {
        if (start.toLocalDate().equals(end.toLocalDate())) {
            // в пределах одного дня
            saveSegment(a, b, start, end);
        } else {
            // первая часть до конца дня
            LocalDateTime endOfDay = LocalDateTime.of(start.toLocalDate(), LocalTime.MAX)
                    .truncatedTo(ChronoUnit.SECONDS);
            saveSegment(a, b, start, endOfDay);
            // вторая часть от начала следующего дня
            LocalDateTime startNext = endOfDay.plusSeconds(1);
            saveSegment(a, b, startNext, end);
        }
    }

    /**
     * Сохраняет один сегмент звонка: исходящий и входящий.
     */
    private void saveSegment(Caller a, Caller b,
                             LocalDateTime s, LocalDateTime e) {
        CdrData out = new CdrData();
        out.setCallType(CallType.OUTGOING);
        out.setCallerNumber(a.getCallerNumber());
        out.setContactNumber(b.getCallerNumber());
        out.setStartTime(s);
        out.setEndTime(e);

        CdrData in = new CdrData();
        in.setCallType(CallType.INCOMING);
        in.setCallerNumber(b.getCallerNumber());
        in.setContactNumber(a.getCallerNumber());
        in.setStartTime(s);
        in.setEndTime(e);

        cdrRepo.saveAll(List.of(out, in));
    }

    /**
     * Простая структура интервала для хранения начального и конечного времени.
     */
    private static class Interval {
        final LocalDateTime start, end;
        Interval(LocalDateTime s, LocalDateTime e) { this.start = s; this.end = e; }
    }
}

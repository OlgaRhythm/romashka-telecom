package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.Caller;
import com.romashka.romashka_telecom.entity.CdrData;
import com.romashka.romashka_telecom.enums.CallType;
import com.romashka.romashka_telecom.repository.CallerRepository;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CallsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

@Service
public class CallsServiceImpl implements CallsService {

    private final CallerRepository callerRepo;
    private final CdrDataRepository cdrRepo;
    private final Random rnd = new Random();

    @Value("${thread.pool.size:4}")
    private int threadPoolSize;

    /** Пара звонков даёт 2 записи => totalPairs*2 записей */
    @Value("${calls.total_pairs:3000}")
    private int totalPairs;

    @Autowired
    public CallsServiceImpl(CallerRepository cR, CdrDataRepository dR) {
        this.callerRepo = cR;
        this.cdrRepo    = dR;
    }

    @Override
    public void generateCalls() {
        List<Caller> callers = callerRepo.findAll();
        if (callers.size() < 2)
            throw new IllegalStateException("Нужно ≥2 абонента");

        // 1) Генерируем и сортируем слоты времени
        List<LocalDateTime> slots = new ArrayList<>(totalPairs);
        for (int i = 0; i < totalPairs; i++) {
            slots.add(randomDateTimeInYear());
        }
        slots.sort(Comparator.naturalOrder());

        // 2) Подготовка синхронизации: теперь листы интервалов, а не один timestamp
        ConcurrentMap<String, List<Interval>> busyIntervals = new ConcurrentHashMap<>();
        ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
        for (Caller c : callers) {
            busyIntervals.put(c.getCallerNumber(), new ArrayList<>());
            locks.put(c.getCallerNumber(), new ReentrantLock());
        }

        // 3) Пул для записи в БД
        ExecutorService exec = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<?>> futures = new ArrayList<>();

        // 4) Каждый слот — в своё generateAt
        for (LocalDateTime start : slots) {
            futures.add(exec.submit(() -> {
                generateAt(start, callers, busyIntervals, locks);
            }));
        }

        // 5) Ждём сохранения
        for (Future<?> f : futures) {
            try { f.get(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }
        exec.shutdown();
    }

    /** Генерирует ровно одну пару звонков в заданный момент start */
    private void generateAt(
            LocalDateTime start,
            List<Caller> callers,
            ConcurrentMap<String, List<Interval>> busyIntervals,
            Map<String, ReentrantLock> locks
    ) {
        while (true) {
            Caller a = callers.get(rnd.nextInt(callers.size()));
            Caller b;
            do { b = callers.get(rnd.nextInt(callers.size())); }
            while (b.getCallerNumber().equals(a.getCallerNumber()));

            String an = a.getCallerNumber(), bn = b.getCallerNumber();
            // Сначала меньший ключ, чтобы не было deadlock
            String k1 = an.compareTo(bn) < 0 ? an : bn;
            String k2 = an.compareTo(bn) < 0 ? bn : an;
            ReentrantLock l1 = locks.get(k1), l2 = locks.get(k2);

            l1.lock(); l2.lock();
            try {
                long dur = 60 + rnd.nextInt(14 * 60);
                LocalDateTime end = start.plusSeconds(dur);

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
            // иначе пробуем снова
        }
    }

    private boolean overlapsAny(List<Interval> list, LocalDateTime s, LocalDateTime e) {
        for (Interval iv : list) {
            if (s.isBefore(iv.end) && e.isAfter(iv.start)) {
                return true;
            }
        }
        return false;
    }

    // Простой DTO для интервалов
    private static class Interval {
        final LocalDateTime start, end;
        Interval(LocalDateTime s, LocalDateTime e) { this.start = s; this.end = e; }
    }

    private LocalDateTime randomDateTimeInYear() {
        LocalDate base = LocalDate.now().withDayOfYear(1);
        int day  = rnd.nextInt(base.lengthOfYear());
        LocalDate d = base.plusDays(day);
        LocalTime t = LocalTime.of(rnd.nextInt(24), rnd.nextInt(60), rnd.nextInt(60));
        return LocalDateTime.of(d, t);
    }

    /** Разбивает на две записи, если звонок проходит через полночь */
    private void savePair(Caller a, Caller b, LocalDateTime start, LocalDateTime end) {
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

    /** Сохраняет один исходящий + один входящий сегмент */
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
}

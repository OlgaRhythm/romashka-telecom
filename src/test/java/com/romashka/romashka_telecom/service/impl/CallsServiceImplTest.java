    package com.romashka.romashka_telecom.service.impl;

    import com.romashka.romashka_telecom.entity.CdrData;
    import com.romashka.romashka_telecom.repository.CallerRepository;
    import com.romashka.romashka_telecom.repository.CdrDataRepository;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;

    import java.util.Comparator;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.ArrayList;

    import static org.junit.jupiter.api.Assertions.assertTrue;

    @SpringBootTest
    public class CallsServiceImplTest {

        @Autowired
        private CallsServiceImpl callsService;

        @Autowired
        private CallerRepository callerRepository;

        @Autowired
        private CdrDataRepository cdrDataRepository;

        @Test
        public void testNoOverlappingCallsPerSubscriber() throws InterruptedException {
            // Генерируем звонки
            callsService.generateCalls();

            // Даём время асинхронным задачам завершиться
            Thread.sleep(10_000); // 10 секунд (можно увеличить при больших объёмах)

            // Получаем все звонки
            List<CdrData> allCalls = cdrDataRepository.findAll();

            // Мапа: номер -> список звонков (включая входящие и исходящие)
            Map<String, List<CdrData>> callsBySubscriber = new HashMap<>();

            for (CdrData call : allCalls) {
                callsBySubscriber.computeIfAbsent(call.getCallerNumber(), k -> new ArrayList<>()).add(call);
                callsBySubscriber.computeIfAbsent(call.getContactNumber(), k -> new ArrayList<>()).add(call);
            }

            // Проверяем, что звонки у одного абонента не пересекаются
            for (Map.Entry<String, List<CdrData>> entry : callsBySubscriber.entrySet()) {
                String number = entry.getKey();
                List<CdrData> calls = entry.getValue();

                // Сортируем по времени начала
                calls.sort(Comparator.comparing(CdrData::getStartTime));

                for (int i = 1; i < calls.size(); i++) {
                    CdrData previous = calls.get(i - 1);
                    CdrData current = calls.get(i);

                    // Проверим, что нет пересечения звонков
                    assertTrue(current.getStartTime().isAfter(previous.getEndTime()) ||
                                    current.getStartTime().isEqual(previous.getEndTime()),
                            String.format("Пересечение звонков у %s:\n%s - %s\n%s - %s",
                                    number,
                                    previous.getStartTime(), previous.getEndTime(),
                                    current.getStartTime(), current.getEndTime()
                            ));
                }
            }
        }
    }
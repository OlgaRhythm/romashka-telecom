package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.Caller;
import com.romashka.romashka_telecom.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.repository.CallerRepository;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CallsGenerationServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CallerRepository callerRepo;

    @Mock
    private CdrDataRepository cdrRepo;

    @InjectMocks
    private CallsGenerationServiceImpl callsGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Задаем пул потоков с нужным количеством потоков
        int threadPoolSize = 8; // Можно заменить на нужное количество
        callsGenerationService.setThreadPoolSize(threadPoolSize); // Предположим, что у тебя есть такой метод в CallsGenerationServiceImpl
    }

//    @Test
//    void testGenerateCalls_withMultipleCallers() throws InterruptedException {
//        // Создаем мокированных абонентов
//        Caller caller1 = new Caller("12345");
//        Caller caller2 = new Caller("67890");
//        when(callerRepo.findAll()).thenReturn(Arrays.asList(caller1, caller2));
//
//        // Подготовка ожидания завершения всех потоков
//        int totalPairs = 10;
//        CountDownLatch latch = new CountDownLatch(totalPairs);
//
//        // Мокируем вызов сохранения CDR данных
//        doAnswer(invocation -> {
//            latch.countDown(); // уменьшаем счетчик после каждого вызова
//            return null;
//        }).when(cdrRepo).saveAll(anyList());
//
//        // Запуск метода генерации звонков
//        callsGenerationService.generateCalls();
//
//        // Ждем завершения всех задач
//        latch.await();
//
//        // Проверка, что событие завершения было вызвано
//        verify(eventPublisher, times(1)).publishEvent(any(CallsGenerationCompletedEvent.class));
//    }

    @Test
    void testGenerateCalls_withLessThanTwoCallers() {
        when(callerRepo.findAll()).thenReturn(Arrays.asList(new Caller("12345")));

        // Ожидаем исключение, так как абонентов меньше 2
        assertThrows(IllegalStateException.class, () -> callsGenerationService.generateCalls());
    }
}

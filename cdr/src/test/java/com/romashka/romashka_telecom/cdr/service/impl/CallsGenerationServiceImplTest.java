package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.cdr.entity.Caller;
import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.cdr.repository.CallerRepository;
import com.romashka.romashka_telecom.cdr.repository.CdrDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CallsGenerationServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CallerRepository callerRepo;

    @Mock
    private CdrDataRepository cdrRepo;

    @Captor
    private ArgumentCaptor<List<CdrData>> cdrDataCaptor;

    @Captor
    private ArgumentCaptor<CallsGenerationCompletedEvent> eventCaptor;

    @InjectMocks
    private CallsGenerationServiceImpl callsGenerationService;

    private List<Caller> testCallers;

    @BeforeEach
    void setUp() {
        // Настройка тестовых данных: 3 абонента
        testCallers = Arrays.asList(
                new Caller("79111111111"),
                new Caller("79222222222"),
                new Caller("79333333333")
        );

        // Настройка параметров сервиса
        callsGenerationService.setThreadPoolSize(4);
        callsGenerationService.setTotalPairs(2); // 2 пары звонков (4 записи)

        // Мокируем вызовы репозиториев
        when(callerRepo.findAll()).thenReturn(testCallers);
    }

    @Test
    void testGenerateCalls_SuccessfulGeneration() {
        // Запуск генерации
        callsGenerationService.generateCalls();

        // Проверяем, что были запрошены абоненты
        verify(callerRepo, times(1)).findAll();

        // Проверяем, что CDR-записи были сохранены
        verify(cdrRepo, times(2)).saveAll(cdrDataCaptor.capture());
        List<List<CdrData>> allSavedCdrs = cdrDataCaptor.getAllValues();
        int totalRecords = allSavedCdrs.stream().mapToInt(List::size).sum();

        // Проверяем количество записей
        assertEquals(4, totalRecords, "Должно быть 4 записи (2 пары звонков)");

        // Проверяем корректность записей
        for (List<CdrData> savedCdrs : allSavedCdrs) {
            for (CdrData cdr : savedCdrs) {
                assertNotNull(cdr.getCallerNumber(), "Номер звонящего не должен быть null");
                assertNotNull(cdr.getContactNumber(), "Номер принимающего не должен быть null");
                assertNotNull(cdr.getStartTime(), "Время начала не должно быть null");
                assertNotNull(cdr.getEndTime(), "Время окончания не должно быть null");
                assertTrue(cdr.getEndTime().isAfter(cdr.getStartTime()),
                        "Время окончания должно быть позже времени начала");
            }
        }


        // Проверяем, что было отправлено событие о завершении
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        CallsGenerationCompletedEvent event = eventCaptor.getValue();
        assertEquals(4, event.getGeneratedRecords(),
                "Количество сгенерированных записей в событии должно быть 4");

        System.out.println(allSavedCdrs);
    }

    @Test
    void testGenerateCalls_withLessThanTwoCallers() {
        // Настраиваем мок для возврата менее 2 абонентов
        when(callerRepo.findAll()).thenReturn(Arrays.asList(new Caller("79111111111")));

        // Проверяем, что при недостаточном количестве абонентов выбрасывается исключение
        assertThrows(IllegalStateException.class, 
                () -> callsGenerationService.generateCalls(),
                "Должно быть выброшено исключение при недостаточном количестве абонентов");
    }
}

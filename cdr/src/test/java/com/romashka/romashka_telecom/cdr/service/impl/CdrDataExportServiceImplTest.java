package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.cdr.config.TimeProperties;
import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.repository.CdrDataRepository;
import com.romashka.romashka_telecom.cdr.service.CdrDataSerializerService;
import com.romashka.romashka_telecom.cdr.service.impl.CdrDataExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
public class CdrDataExportServiceImplTest {
    @Mock
    private CdrDataRepository cdrRepo;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CdrDataSerializerService serializer;
    @Mock
    private TaskScheduler scheduler;

    @Mock
    private TimeProperties timeProperties;

    @InjectMocks
    private CdrDataExportServiceImpl exportService;

    @Captor
    private ArgumentCaptor<String> csvCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(timeProperties.getStart()).thenReturn(LocalDateTime.of(2025,1,1,0,0));
        when(timeProperties.getEnd()).thenReturn(LocalDateTime.of(2025,12,31,23,59));
        when(timeProperties.getCoefficient()).thenReturn(1.0);

        // scheduler сразу выполняет задачу
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class), any(Instant.class));


        // задаём приватные поля через reflection
        setField(exportService, "exchangeName", "test-exchange");
        setField(exportService, "routingKey", "test-routing-key");
        setField(exportService, "batchSize", 2);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = CdrDataExportServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void exportCallsData_shouldExportInBatchesAndSendToRabbit() {
        // Arrange
        List<CdrData> dataList = Arrays.asList(new CdrData(), new CdrData(), new CdrData());
        when(cdrRepo.streamAllBy(Sort.by("startTime").ascending())).thenReturn(dataList.stream());
        when(serializer.convertToCsv(anyList())).thenReturn("csv-data");

        dataList.forEach(d -> d.setEndTime(LocalDateTime.of(2025,1,1,0,0)));
        // Act
        exportService.exportCallsData();

        // Assert
        verify(serializer, times(1)).convertToCsv(anyList());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("test-exchange"), eq("test-routing-key"),
                eq("csv-data"), any(MessagePostProcessor.class));
    }

    @Test
    void exportCallsData_shouldHandleEmptyDataGracefully() {
        when(cdrRepo.streamAllBy(any())).thenReturn(Stream.empty());

        exportService.exportCallsData();

        verifyNoInteractions(serializer);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void exportCallsData_shouldCatchExceptionOnSend() {
        List<CdrData> dataList = List.of(new CdrData(), new CdrData());
        when(cdrRepo.streamAllBy(any())).thenReturn(dataList.stream());
        when(serializer.convertToCsv(anyList())).thenReturn("csv-data");
        doThrow(new RuntimeException("RabbitMQ is down")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));

        dataList.forEach(d -> d.setEndTime(LocalDateTime.of(2025,1,1,0,0)));

        exportService.exportCallsData();

        verify(serializer).convertToCsv(anyList());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
    }
}

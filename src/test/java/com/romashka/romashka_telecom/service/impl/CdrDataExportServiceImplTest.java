package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.CdrData;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CdrDataSerializerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;

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

    @InjectMocks
    private CdrDataExportServiceImpl exportService;

    @Captor
    private ArgumentCaptor<String> csvCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exportService = new CdrDataExportServiceImpl(cdrRepo, rabbitTemplate, serializer);

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

        // Act
        exportService.exportCallsData();

        // Assert
        verify(serializer, times(2)).convertToCsv(anyList());
        verify(rabbitTemplate, times(2)).convertAndSend(eq("test-exchange"), eq("test-routing-key"),
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
                .convertAndSend(anyString(), anyString(), anyString(), any(CorrelationData.class));

        exportService.exportCallsData();

        verify(serializer).convertToCsv(anyList());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
    }
}

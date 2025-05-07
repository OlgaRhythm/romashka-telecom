package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.enums.CallType;
import com.romashka.romashka_telecom.cdr.service.CdrDataSerializerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ExportCdrRToRabbitTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CdrDataSerializerService serializer;

    @InjectMocks
    private ExportCdrRToRabbit exportService;

    @Captor
    private ArgumentCaptor<String> csvContentCaptor;

    @TempDir
    Path tempDir;

    private Path testCsvFile;
    private static final String TEST_EXCHANGE = "cdr.exchange";
    private static final String TEST_ROUTING_KEY = "cdr.routingkey";

    @BeforeEach
    void setUp() throws IOException {
        // Создаем тестовый CSV файл
        testCsvFile = tempDir.resolve("test.csv");
        String csvContent = "call_type,caller_number,contact_number,start_time,end_time\n" +
                "OUTGOING,79001112233,79872332221,2025-01-01T10:00:00,2025-01-01T10:05:00\n" +
                "INCOMING,79872332221,79001112233,2025-01-01T11:00:00,2025-01-01T11:03:00";
        Files.writeString(testCsvFile, csvContent);

        // Устанавливаем тестовые значения для полей
        setField(exportService, "exchangeName", TEST_EXCHANGE);
        setField(exportService, "routingKey", TEST_ROUTING_KEY);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = ExportCdrRToRabbit.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void exportCsvToRabbit_shouldSuccessfullyExportValidCsv() throws IOException {
        // Arrange
        String expectedSerializedCsv = "serialized,csv,content";
        when(serializer.convertToCsv(any())).thenReturn(expectedSerializedCsv);

        // Act
        exportService.exportCsvToRabbit(testCsvFile);

        // Assert
        verify(serializer).convertToCsv(any());
        verify(rabbitTemplate).convertAndSend(
                eq(TEST_EXCHANGE),
                eq(TEST_ROUTING_KEY),
                eq(expectedSerializedCsv)

        );
    }

    @Test
    void exportCsvToRabbit_shouldHandleEmptyFile() throws IOException {
        // Arrange
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "call_type,caller_number,contact_number,start_time,end_time\n");

        // Act
        exportService.exportCsvToRabbit(emptyFile);

        // Assert
        verifyNoInteractions(serializer);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void exportCsvToRabbit_shouldHandleInvalidCsvFormat() throws IOException {
        // Arrange
        Path invalidFile = tempDir.resolve("invalid.csv");
        Files.writeString(invalidFile, "invalid,format\nline1\nline2");

        // Act & Assert
        assertDoesNotThrow(() -> exportService.exportCsvToRabbit(invalidFile));
        verifyNoInteractions(serializer);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void exportCsvToRabbit_shouldHandleRabbitMqError() throws IOException {
        // Arrange
        String expectedSerializedCsv = "serialized,csv,content";
        when(serializer.convertToCsv(any())).thenReturn(expectedSerializedCsv);
        doThrow(new RuntimeException("RabbitMQ error"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> exportService.exportCsvToRabbit(testCsvFile));
        verify(serializer).convertToCsv(any());
        verify(rabbitTemplate).convertAndSend(
                eq(TEST_EXCHANGE),
                eq(TEST_ROUTING_KEY),
                eq(expectedSerializedCsv)

        );
    }

    @Test
    void exportCsvToRabbit_shouldCorrectlyParseCsvData() throws IOException {
        // Arrange
        String csvContent = "call_type,caller_number,contact_number,start_time,end_time\n" +
                "OUTGOING,1234567890,0987654321,2025-01-01T10:00:00,2025-01-01T10:05:00";
        Path testFile = tempDir.resolve("test_parse.csv");
        Files.writeString(testFile, csvContent);

        when(serializer.convertToCsv(any())).thenAnswer(invocation -> {
            List<CdrData> data = invocation.getArgument(0);
            assertEquals(1, data.size());
            CdrData cdr = data.get(0);
            assertEquals(CallType.OUTGOING, cdr.getCallType());
            assertEquals("1234567890", cdr.getCallerNumber());
            assertEquals("0987654321", cdr.getContactNumber());
            assertEquals(LocalDateTime.parse("2025-01-01T10:00:00"), cdr.getStartTime());
            assertEquals(LocalDateTime.parse("2025-01-01T10:05:00"), cdr.getEndTime());
            return "serialized";
        });

        // Act
        exportService.exportCsvToRabbit(testFile);

        // Assert
        verify(serializer).convertToCsv(any());
        verify(rabbitTemplate).convertAndSend(
                eq(TEST_EXCHANGE),
                eq(TEST_ROUTING_KEY),
                eq("serialized")

        );
    }
} 
package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.cdr.service.CdrDataSerializerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class ExportCdrRToRabbitIntegrationTest {

    @Autowired
    private ExportCdrRToRabbit exportService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    //private CdrDataSerializerService serializer;

    @TempDir
    Path tempDir;

    private Path testCsvFile;



    @BeforeEach
    void setUp() throws IOException {
        // Создаем тестовый CSV файл
        testCsvFile = tempDir.resolve("test.csv");
        String csvContent = "call_type,caller_number,contact_number,start_time,end_time\n" +
                "OUTGOING,79001112233,79872332221,2025-01-01T10:00:00,2025-01-01T10:05:00\n" +
                "INCOMING,79872332221,79001112233,2025-01-01T11:00:00,2025-01-01T11:03:00";
        Files.writeString(testCsvFile, csvContent);


    }

    @Test
    void shouldSendMessageToRabbitMQ() throws IOException, InterruptedException {
        // Act
        Path csvFile = Path.of("resources/cdr/ValidCdr.csv");
        exportService.exportCsvToRabbit(testCsvFile);

        // Даем время на отправку сообщения
        Thread.sleep(1000);

        // Assert
        Message receivedMessage = rabbitTemplate.receive("cdr.queue", 5000);
        assertNotNull(receivedMessage, "Message should be received from RabbitMQ");
        
        MessageProperties properties = receivedMessage.getMessageProperties();
        assertEquals("text/csv", properties.getContentType());
        assertEquals("UTF-8", properties.getContentEncoding());
        
        String receivedContent = new String(receivedMessage.getBody());
        assertTrue(receivedContent.contains("OUTGOING"));
        assertTrue(receivedContent.contains("INCOMING"));
    }
} 
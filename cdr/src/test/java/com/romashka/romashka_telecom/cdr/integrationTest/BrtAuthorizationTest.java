package com.romashka.romashka_telecom.cdr.integrationTest;

import com.romashka.romashka_telecom.cdr.util.ContainerLogReader;
import com.romashka.romashka_telecom.cdr.util.ExportCdrRToRabbit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class BrtAuthorizationTest {
    @Autowired
    private ExportCdrRToRabbit exportCdrRToRabbit;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ContainerLogReader containerLogReader;

    //private CdrCsvParser cdrCsvParser;

    private static final String VALID_FILE_PATH = "test-data/ValidCdr.csv";
    private static final String QUEUE_NAME = "cdr.queue";

    @BeforeEach
    void setUp() throws IOException {
        // Очищаем очередь перед каждым тестом
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(QUEUE_NAME);
            return null;
        });
        Path csvFile = new ClassPathResource(VALID_FILE_PATH).getFile().toPath();
        exportCdrRToRabbit.sendCsvToRabbit(csvFile);
    }

    @Test
    void shouldSendCsvToRabbit() throws IOException {


    }




}

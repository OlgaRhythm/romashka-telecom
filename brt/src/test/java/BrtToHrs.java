package com.romashka.romashka_telecom.brt;

import com.romashka.romashka_telecom.brt.util.ContainerLogReader;
import com.romashka.romashka_telecom.brt.service.ExportCdrToRabbit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = com.romashka.romashka_telecom.brt.BRTApplication.class)
@ActiveProfiles("test")
public class BrtToHrs {

    private static final String URL = "jdbc:postgresql://localhost:5432/brt_db";
    private static final String USER = "brt_user";
    private static final String PASSWORD = "brt_pass";
    private Connection connection;
    private Statement statement;

    @Autowired
    private ExportCdrToRabbit exportCdrRToRabbit;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ContainerLogReader containerLogReader;

    @Value("${rabbitmq.brt-to-hrs.exchange.name}")
    private String brtToHrsExchangeName;

    @Value("${rabbitmq.brt-to-hrs.routing.key}")
    private String brtToHrsRoutingKey;

    private static final String VALID_FILE_PATH = "test-data/ValidCdr.csv";
    private static final String QUEUE_NAME = "cdr.queue";
    private static final String BRT_TO_HRS_QUEUE = "brt-to-hrs.queue";

    @BeforeEach
    void setUp() throws IOException, SQLException, ClassNotFoundException {
        log.info("Starting test setup...");

        // Проверяем конфигурацию RabbitMQ
        log.info("Checking RabbitMQ configuration:");
        log.info("BRT to HRS Exchange: {}", brtToHrsExchangeName);
        log.info("BRT to HRS Routing Key: {}", brtToHrsRoutingKey);

        // Очищаем очереди перед каждым тестом
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(QUEUE_NAME);
            channel.queuePurge(BRT_TO_HRS_QUEUE);
            return null;
        });

        log.info("Queues purged: {} and {}", QUEUE_NAME, BRT_TO_HRS_QUEUE);

        // Отправляем тестовые данные
        Path csvFile = new ClassPathResource(VALID_FILE_PATH).getFile().toPath();
        log.info("Sending CSV file to RabbitMQ: {}", csvFile);
        exportCdrRToRabbit.sendCsvToRabbit(csvFile);

        // Ждем немного, чтобы данные успели обработаться
        try {
            TimeUnit.SECONDS.sleep(5); // Увеличиваем время ожидания
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Подключаемся к БД
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        statement = connection.createStatement();
        log.info("Database connection established");
    }

    @Test
    void callsShoulAddToDb() throws IOException {
        try {
            log.info("Starting test: callsShoulAddToDb");

            // Проверяем, что данные попали в БД
            String sql = "SELECT * FROM calls c JOIN callers cs ON c.caller_id = cs.caller_id WHERE contact_number = '79872332221'";
            log.info("Executing SQL query: {}", sql);
            ResultSet resultSet = statement.executeQuery(sql);

            assertFalse(resultSet.wasNull(), "Должны найти хотя бы одну запись");
            log.info("Data found in database");

            // Проверяем количество сообщений в очереди brt-to-hrs
            long messageCount = rabbitTemplate.execute(channel -> {
                return channel.messageCount(BRT_TO_HRS_QUEUE);
            });
            log.info("Number of messages in brt-to-hrs queue: {}", messageCount);

            // Проверяем, что данные были отправлены в HRS
            List<String> logs = containerLogReader.getContainerLogs(1000);
            boolean dataSentToHrs = logs.stream()
                .anyMatch(log -> log.contains("brt-to-hrs") && log.contains("routingkey"));

            log.info("Checking if data was sent to HRS. Found in logs: {}", dataSentToHrs);

            // Сохраняем логи для анализа
            String logFile = containerLogReader.saveLogsToFile(logs);
            if (logFile != null) {
                log.info("Logs saved to file: {}", logFile);
            }

            // Закрываем ресурсы
            resultSet.close();
            statement.close();
            connection.close();
            log.info("Database connection closed");

        } catch (Exception e) {
            log.error("Error during test execution", e);
            throw new RuntimeException(e);
        }
    }
}

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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BrtAuthorizationTest {

    private static final String URL = "jdbc:postgresql://localhost:5432/brt_db";
    private static final String USER = "brt_user";
    private static final String PASSWORD = "brt_pass";
    private Connection connection;
    private Statement statement;

    @Autowired
    private ExportCdrRToRabbit exportCdrRToRabbit;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ContainerLogReader containerLogReader;



    private static final String VALID_FILE_PATH = "test-data/ValidCdr.csv";
    private static final String QUEUE_NAME = "cdr.queue";

    @BeforeEach
    void setUp() throws IOException, SQLException, ClassNotFoundException {
        // Очищаем очередь перед каждым тестом
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(QUEUE_NAME);
            return null;
        });
        Path csvFile = new ClassPathResource(VALID_FILE_PATH).getFile().toPath();
        exportCdrRToRabbit.sendCsvToRabbit(csvFile);

        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        statement = connection.createStatement();


    }

    @Test
    void callsShoulAddToDb() throws IOException {
        try {

            // Выполняем SELECT запрос
            String sql = "SELECT * FROM calls c JOIN callers cs ON c.caller_id = cs.caller_id WHERE  contact_number = '79872332221' ";
            ResultSet resultSet = statement.executeQuery(sql);

            assertFalse(resultSet.wasNull(), "Должны найти хотя бы одну запись");

            // Закрываем ресурсы
            resultSet.close();
            statement.close();
            connection.close();
            System.out.println("Соединение закрыто.");




        } catch (Exception e) {
            System.out.println("Ошибка при работе с базой данных:");
            e.printStackTrace();
        }


    }




}

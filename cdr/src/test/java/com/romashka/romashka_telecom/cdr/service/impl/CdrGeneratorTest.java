package com.romashka.romashka_telecom.cdr.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
//import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.cdr.entity.Caller;
import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.enums.CallType;
import com.romashka.romashka_telecom.cdr.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.cdr.repository.CallerRepository;
import com.romashka.romashka_telecom.cdr.repository.CdrDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CdrGeneratorTest {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Random RANDOM = new Random();
    private static final int MIN_CALL_DURATION = 60; // 1 minute
    private static final int MAX_CALL_DURATION = 14 * 60; // 14 minutes
    private static final int MAX_RECORDS_PER_FILE = 10;

    private static final String URL = "jdbc:postgresql://localhost:5432/brt_db";
    private static final String USER = "brt_user";
    private static final String PASSWORD = "brt_pass";

    @TempDir
    Path tempDir;

    @Mock
    private CdrDataExportServiceImpl cdrExport;

    @Mock
    private CdrDataSerializerServiceImpl cdrSerializer;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CallerRepository callerRepo;

    @InjectMocks
    private CallsGenerationServiceImpl callsGenerationService;

    @Mock
    private CdrDataRepository cdrRepo;

    @Captor
    private ArgumentCaptor<List<CdrData>> cdrDataCaptor;

    @Captor
    private ArgumentCaptor<CallsGenerationCompletedEvent> eventCaptor;

    @Captor
    private  ArgumentCaptor<List<CdrData>> csvFileCaptor;

    Path resourcesPath = Paths.get("brt", "src", "test", "resources", "test-data");

    private List<Caller> testCallers;

    @BeforeEach
    void setUp() {

        testCallers = callerGenerator(5);
        //tempDir = Path.of("brt/src/test/resources/test-data");

        File resourcesDir = resourcesPath.toFile();
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }


        callsGenerationService.setThreadPoolSize(4);
        callsGenerationService.setTotalPairs(10); // 10 пары звонков (20 записи)
    }




    void generateCorrectCdrFilesTest() throws IOException {

        when(callerRepo.findAll()).thenReturn(testCallers);

        callsGenerationService.generateCalls();

        // Проверяем, что были запрошены абоненты
        verify(callerRepo, times(1)).findAll();

        // Проверяем, что CDR-записи были сохранены
        verify(cdrRepo, times(10)).saveAll(cdrDataCaptor.capture());
        List<List<CdrData>> allSavedCdrs = cdrDataCaptor.getAllValues();
        int totalRecords = allSavedCdrs.stream().mapToInt(List::size).sum();

        // Проверяем количество записей
        assertEquals(20, totalRecords, "Должно быть 20 записи (10 пары звонков)");



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
        assertEquals(20, event.getGeneratedRecords(),
                "Количество сгенерированных записей в событии должно быть 10");

        // Создаем файлы по 10 записей в каждом
        int fileCounter = 1;
        int recordsInCurrentFile = 0;
        FileWriter currentWriter = null;

        try {
            for (List<CdrData> savedCdrs : allSavedCdrs) {
                for (CdrData cdr : savedCdrs) {

                    if (recordsInCurrentFile == 0) {

                        if (currentWriter != null) {
                            currentWriter.close();
                        }

                        File csvFile = tempDir.resolve(String.format("test_cdr_%d.csv", fileCounter)).toFile();
                        currentWriter = new FileWriter(csvFile);

                        currentWriter.write("call_type,caller_number,contact_number,start_time,end_time\n");
                        fileCounter++;
                    }

                    // Записываем запись
                    currentWriter.write(String.format("%s,%s,%s,%s,%s%n",
                            cdr.getCallType().getCode(),
                            cdr.getCallerNumber(),
                            cdr.getContactNumber(),
                            cdr.getStartTime().format(DTF),
                            cdr.getEndTime().format(DTF)
                    ));

                    recordsInCurrentFile++;


                    if (recordsInCurrentFile == 10) {
                        recordsInCurrentFile = 0;
                    }
                }
            }
        } finally {

            if (currentWriter != null) {
                currentWriter.close();
            }
        }


        for (int i = 1; i < fileCounter; i++) {
            File csvFile = tempDir.resolve(String.format("test_cdr_%d.csv", i)).toFile();
            assertTrue(csvFile.exists(), "CSV file " + i + " should be created");
            assertTrue(csvFile.length() > 0, "CSV file " + i + " should not be empty");


            System.out.println("Generated CSV file " + i + " contents:");
            System.out.println("----------------------------");
            Files.readAllLines(csvFile.toPath()).forEach(System.out::println);
            System.out.println("----------------------------");
        }

    }




    @Test
    void generateIncorrectTimeCdrFiles() throws IOException {

        List<CdrData> testData = generateTestDataWithIncorrectTime(5);

        cdrExport.scheduleCsvBatch(cdrSerializer.convertToCsv(testData), testData);

        try {
            // Регистрируем драйвер PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Устанавливаем соединение с базой данных
            System.out.println("Подключение к базе данных...");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Подключение успешно установлено!");

            // Создаем Statement для выполнения запросов
            Statement statement = connection.createStatement();

            // Выполняем SELECT запрос
            String sql = "SELECT number FROM callers WHERE number = [testData.get(0).getCallerNumber()]";
            ResultSet resultSet = statement.executeQuery(sql);

            // Обрабатываем результаты
            System.out.println("\nРезультаты запроса:");
            System.out.println("------------------------");
            while (resultSet.next()) {
                System.out.println("Caller ID: " + resultSet.getLong("caller_id"));
                System.out.println("Number: " + resultSet.getString("number"));
                System.out.println(resultSet.getString("rate_date"));
                System.out.println(resultSet.getLong("rate_id"));
                System.out.println(resultSet.getLong("subscriber_id"));
                System.out.println("------------------------");
            }

            assertEquals(testData.get(0).getCallerNumber(), resultSet.getString("number"));

            // Закрываем ресурсы
            resultSet.close();
            statement.close();
            connection.close();
            System.out.println("Соединение закрыто.");

        } catch (Exception e) {
            System.out.println("Ошибка при работе с базой данных:");
            e.printStackTrace();
        }

        List<List<CdrData>> chunks = new ArrayList<>();
//        for (int i = 0; i < testData.size(); i += MAX_RECORDS_PER_FILE) {
//            chunks.add(testData.subList(i, Math.min(i + MAX_RECORDS_PER_FILE, testData.size())));
//        }
//
//
//        for (int i = 0; i < chunks.size(); i++) {
//            List<CdrData> chunk = chunks.get(i);
//
//            File csvFile = resourcesPath.resolve(String.format("IncorrectTimeCdrFile_%d.csv", i + 1)).toFile();
//
//            try (FileWriter writer = new FileWriter(csvFile)) {
//
//                writer.write("call_type,caller_number,contact_number,start_time,end_time\n");
//
//
//                for (CdrData cdr : chunk) {
//                    writer.write(String.format("%s,%s,%s,%s,%s%n",
//                            cdr.getCallType().getCode(),
//                            cdr.getCallerNumber(),
//                            cdr.getContactNumber(),
//                            cdr.getStartTime().format(DTF),
//                            cdr.getEndTime().format(DTF)
//                    ));
//                }
//            }


//            assertTrue(csvFile.exists(), "CSV file should be created");
//            assertTrue(csvFile.length() > 0, "CSV file should not be empty");
//
//
//            System.out.println("Generated CSV file " + (i + 1) + " contents:");
//            System.out.println("----------------------------");
//            Files.readAllLines(csvFile.toPath()).forEach(System.out::println);
//            System.out.println("----------------------------");
       // }
    }

    @Test
    void generateIncorrectNumberTestCdrFiles() throws IOException {

        List<CdrData> testData = generateTestDataWithIncorrectNumber(5);


        List<List<CdrData>> chunks = new ArrayList<>();
        for (int i = 0; i < testData.size(); i += MAX_RECORDS_PER_FILE) {
            chunks.add(testData.subList(i, Math.min(i + MAX_RECORDS_PER_FILE, testData.size())));
        }


        for (int i = 0; i < chunks.size(); i++) {
            List<CdrData> chunk = chunks.get(i);
            File csvFile = tempDir.resolve(String.format("test_cdr_%d.csv", i + 1)).toFile();

            try (FileWriter writer = new FileWriter(csvFile)) {

                writer.write("call_type,caller_number,contact_number,start_time,end_time\n");


                for (CdrData cdr : chunk) {
                    writer.write(String.format("%s,%s,%s,%s,%s%n",
                            cdr.getCallType().getCode(),
                            cdr.getCallerNumber(),
                            cdr.getContactNumber(),
                            cdr.getStartTime().format(DTF),
                            cdr.getEndTime().format(DTF)
                    ));
                }
            }


            assertTrue(csvFile.exists(), "CSV file should be created");
            assertTrue(csvFile.length() > 0, "CSV file should not be empty");


            System.out.println("Generated CSV file " + (i + 1) + " contents:");
            System.out.println("----------------------------");
            Files.readAllLines(csvFile.toPath()).forEach(System.out::println);
            System.out.println("----------------------------");
        }
    }

    @Test
    void generateIncorrectSeporatorTestCdrFiles() throws IOException {

        List<CdrData> testData = generateTestData(5);


        List<List<CdrData>> chunks = new ArrayList<>();
        for (int i = 0; i < testData.size(); i += MAX_RECORDS_PER_FILE) {
            chunks.add(testData.subList(i, Math.min(i + MAX_RECORDS_PER_FILE, testData.size())));
        }


        for (int i = 0; i < chunks.size(); i++) {
            List<CdrData> chunk = chunks.get(i);
            File csvFile = tempDir.resolve(String.format("test_cdr_%d.csv", i + 1)).toFile();

            try (FileWriter writer = new FileWriter(csvFile)) {

                writer.write("call_type,caller_number,contact_number,start_time,end_time\n");


                for (CdrData cdr : chunk) {
                    writer.write(String.format("%s;%s;%s;%s;%s%n",
                            cdr.getCallType().getCode(),
                            cdr.getCallerNumber(),
                            cdr.getContactNumber(),
                            cdr.getStartTime().format(DTF),
                            cdr.getEndTime().format(DTF)
                    ));
                }
            }


            assertTrue(csvFile.exists(), "CSV file should be created");
            assertTrue(csvFile.length() > 0, "CSV file should not be empty");


            System.out.println("Generated CSV file " + (i + 1) + " contents:");
            System.out.println("----------------------------");
            Files.readAllLines(csvFile.toPath()).forEach(System.out::println);
            System.out.println("----------------------------");
        }
    }

    @Test
    void generateTestCdrFilesWithMissingParametr() throws IOException {

        List<CdrData> testData = generateTestDataWithMissingParametr(5);


        List<List<CdrData>> chunks = new ArrayList<>();
        for (int i = 0; i < testData.size(); i += MAX_RECORDS_PER_FILE) {
            chunks.add(testData.subList(i, Math.min(i + MAX_RECORDS_PER_FILE, testData.size())));
        }


        for (int i = 0; i < chunks.size(); i++) {
            List<CdrData> chunk = chunks.get(i);
            File csvFile = tempDir.resolve(String.format("test_cdr_%d.csv", i + 1)).toFile();

            try (FileWriter writer = new FileWriter(csvFile)) {

                writer.write("call_type,caller_number,contact_number,start_time,end_time\n");


                for (CdrData cdr : chunk) {
                    writer.write(String.format("%s,%s,%s,%s%n",
                            //cdr.getCallType().getCode(),
                            cdr.getCallerNumber(),
                            cdr.getContactNumber(),
                            cdr.getStartTime().format(DTF),
                            cdr.getEndTime().format(DTF)
                    ));
                }
            }


            assertTrue(csvFile.exists(), "CSV file should be created");
            assertTrue(csvFile.length() > 0, "CSV file should not be empty");


            System.out.println("Generated CSV file " + (i + 1) + " contents:");
            System.out.println("----------------------------");
            Files.readAllLines(csvFile.toPath()).forEach(System.out::println);
            System.out.println("----------------------------");
        }
    }

    private List<CdrData> generateTestData(int numberOfPairs) {
        List<CdrData> result = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < numberOfPairs; i++) {

            String callerNumber = generatePhoneNumber();
            String contactNumber = generatePhoneNumber();
            LocalDateTime startTime = baseTime.plusMinutes(i * 15); // Each pair starts 15 minutes after the previous
            int duration = RANDOM.nextInt(MAX_CALL_DURATION - MIN_CALL_DURATION) + MIN_CALL_DURATION;
            LocalDateTime endTime = startTime.plusSeconds(duration);


            CdrData outgoing = new CdrData();
            outgoing.setCallType(CallType.OUTGOING);
            outgoing.setCallerNumber(callerNumber);
            outgoing.setContactNumber(contactNumber);
            outgoing.setStartTime(startTime);
            outgoing.setEndTime(endTime);
            result.add(outgoing);


            CdrData incoming = new CdrData();
            incoming.setCallType(CallType.INCOMING);
            incoming.setCallerNumber(contactNumber);
            incoming.setContactNumber(callerNumber);
            incoming.setStartTime(startTime);
            incoming.setEndTime(endTime);
            result.add(incoming);
        }

        return result;
    }

    private List<CdrData> generateTestDataWithIncorrectTime(int numberOfPairs) {
        List<CdrData> result = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < numberOfPairs; i++) {

            String callerNumber = generatePhoneNumber();
            String contactNumber = generatePhoneNumber();

            int duration = RANDOM.nextInt(MAX_CALL_DURATION - MIN_CALL_DURATION) + MIN_CALL_DURATION;

            LocalDateTime endTime = baseTime.plusMinutes(i * 15);
            LocalDateTime startTime = endTime.plusSeconds(duration); // Each pair starts 15 minutes after the previous


            CdrData outgoing = new CdrData();
            outgoing.setCallType(CallType.OUTGOING);
            outgoing.setCallerNumber(callerNumber);
            outgoing.setContactNumber(contactNumber);
            outgoing.setStartTime(startTime);
            outgoing.setEndTime(endTime);
            result.add(outgoing);


            CdrData incoming = new CdrData();
            incoming.setCallType(CallType.INCOMING);
            incoming.setCallerNumber(contactNumber);
            incoming.setContactNumber(callerNumber);
            incoming.setStartTime(startTime);
            incoming.setEndTime(endTime);
            result.add(incoming);
        }

        return result;
    }

    private List<CdrData> generateTestDataWithIncorrectNumber(int numberOfPairs) {
        List<CdrData> result = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < numberOfPairs; i++) {

            String callerNumber = generatePhoneNumber();
            String contactNumber = generateIncorrectPhoneNumber();
            LocalDateTime startTime = baseTime.plusMinutes(i * 15); // Each pair starts 15 minutes after the previous
            int duration = RANDOM.nextInt(MAX_CALL_DURATION - MIN_CALL_DURATION) + MIN_CALL_DURATION;
            LocalDateTime endTime = startTime.plusSeconds(duration);


            CdrData outgoing = new CdrData();
            outgoing.setCallType(CallType.OUTGOING);
            outgoing.setCallerNumber(callerNumber);
            outgoing.setContactNumber(contactNumber);
            outgoing.setStartTime(startTime);
            outgoing.setEndTime(endTime);
            result.add(outgoing);


            CdrData incoming = new CdrData();
            incoming.setCallType(CallType.INCOMING);
            incoming.setCallerNumber(contactNumber);
            incoming.setContactNumber(callerNumber);
            incoming.setStartTime(startTime);
            incoming.setEndTime(endTime);
            result.add(incoming);
        }

        return result;
    }

    private List<CdrData> generateTestDataWithMissingParametr(int numberOfPairs) {
        List<CdrData> result = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < numberOfPairs; i++) {

            String callerNumber = generatePhoneNumber();
            String contactNumber = generatePhoneNumber();
            LocalDateTime startTime = baseTime.plusMinutes(i * 15); // Each pair starts 15 minutes after the previous
            int duration = RANDOM.nextInt(MAX_CALL_DURATION - MIN_CALL_DURATION) + MIN_CALL_DURATION;
            LocalDateTime endTime = startTime.plusSeconds(duration);


            CdrData outgoing = new CdrData();

            outgoing.setCallerNumber(callerNumber);
            outgoing.setContactNumber(contactNumber);
            outgoing.setStartTime(startTime);
            outgoing.setEndTime(endTime);
            result.add(outgoing);


            CdrData incoming = new CdrData();
            incoming.setCallType(CallType.INCOMING);
            incoming.setCallerNumber(contactNumber);
            incoming.setContactNumber(callerNumber);
            incoming.setStartTime(startTime);
            incoming.setEndTime(endTime);
            result.add(incoming);
        }

        return result;
    }

    private String generateIncorrectPhoneNumber() {

        StringBuilder number = new StringBuilder("7");
        for (int i = 0; i < 5; i++) {
            number.append(RANDOM.nextInt(5));
        }
        return number.toString();
    }

    private String generatePhoneNumber() {

        StringBuilder number = new StringBuilder("7");
        for (int i = 0; i < 10; i++) {
            number.append(RANDOM.nextInt(10));
        }
        return number.toString();
    }

    private List<Caller> callerGenerator(int amount) {
        List<Caller> callers = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            Caller caller = new Caller(i,generatePhoneNumber());
            callers.add(caller);


        }
        return callers;
    }
}
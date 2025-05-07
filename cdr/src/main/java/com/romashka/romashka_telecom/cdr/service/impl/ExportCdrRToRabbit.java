package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.enums.CallType;
import com.romashka.romashka_telecom.cdr.service.CdrDataSerializerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для экспорта CDR-данных из CSV файлов в RabbitMQ.
 * Читает CSV файлы, парсит их и отправляет данные в RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportCdrRToRabbit {

    private final RabbitTemplate rabbitTemplate;
    private final CdrDataSerializerService serializer;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Экспортирует данные из CSV файла в RabbitMQ.
     * Читает файл, парсит его и отправляет данные в RabbitMQ.
     *
     * @param csvFilePath путь к CSV файлу
     * @throws IOException если возникла ошибка при чтении файла
     */
    @Transactional
    public void exportCsvToRabbit(Path csvFilePath) throws IOException {
        log.info("Starting export from CSV file: {}", csvFilePath);

        String csvContent = Files.readString(csvFilePath);
        //List<CdrData> cdrDataList = parseCsvToCdrData(csvContent);
        sendToRabbit(csvContent, csvContent.length());
//        if (!cdrDataList.isEmpty()) {
//            String serializedCsv = serializer.convertToCsv(cdrDataList);
//            sendToRabbit(serializedCsv, cdrDataList.size());
//        }

        log.info("Export completed successfully. Processed {} records", csvContent.length());
    }

    /**
     * Отправляет данные в RabbitMQ.
     *
     * @param csvContent содержимое CSV
     * @param recordCount количество записей
     */
    private void sendToRabbit(String csvContent, int recordCount) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, csvContent, msg -> {
                msg.getMessageProperties().setContentType("text/csv");
                msg.getMessageProperties().setContentEncoding("UTF-8");
                return msg;
            });
            log.info("Successfully sent {} records to RabbitMQ", recordCount);
        } catch (Exception e) {
            log.error("Failed to send data to RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send data to RabbitMQ", e);
        }
    }

    /**
     * Парсит CSV контент в список объектов CdrData.
     *
     * @param csvContent содержимое CSV файла
     * @return список объектов CdrData
     */
    private List<CdrData> parseCsvToCdrData(String csvContent) {
        List<CdrData> result = new ArrayList<>();
        String[] lines = csvContent.split("\n");
        
        // Пропускаем заголовок
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                String[] fields = line.split(",");
                if (fields.length >= 5) {
                    CdrData cdrData = new CdrData();
                    cdrData.setCallType(CallType.valueOf(fields[0]));
                    cdrData.setCallerNumber(fields[1]);
                    cdrData.setContactNumber(fields[2]);
                    cdrData.setStartTime(LocalDateTime.parse(fields[3]));
                    cdrData.setEndTime(LocalDateTime.parse(fields[4]));
                    result.add(cdrData);
                }
            }
        }
        
        return result;
    }
} 
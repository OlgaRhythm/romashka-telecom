package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.CdrData;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CdrDataExportService;
import com.romashka.romashka_telecom.service.CdrDataSerializerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Реализация сервиса {@link CdrDataExportService},
 * отвечающая за экспорт данных CDR (Call Detail Records) в виде CSV
 * и отправку их по RabbitMQ.
 *
 * Данные извлекаются из базы данных, сериализуются в CSV
 * и отправляются партиями по заранее настроенному exchange и routing key.
 *
 * Параметры конфигурируются через application.properties:
 * <ul>
 *     <li><b>rabbitmq.exchange.name</b> — имя RabbitMQ exchange</li>
 *     <li><b>rabbitmq.routing.key</b> — routing key для отправки сообщений</li>
 *     <li><b>export.batch.size</b> — размер одной партии отправки (по умолчанию 10)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CdrDataExportServiceImpl implements CdrDataExportService {

    /** Репозиторий для доступа к CDR-данным */
    private final CdrDataRepository cdrRepo;
    /** Шаблон для отправки сообщений в RabbitMQ */
    private final RabbitTemplate rabbitTemplate;
    /** Сервис сериализации CDR-данных в формат CSV */
    private final CdrDataSerializerService serializer;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${export.batch.size:10}")
    private int batchSize;

    /**
     * Выполняет экспорт данных о звонках:
     * читает данные из базы, сериализует в CSV и отправляет по RabbitMQ партиями.
     */
    @Override
    @Transactional(readOnly = true)
    public void exportCallsData() {
        log.info("Starting exportCallsData, batchSize={}", batchSize);

        try (Stream<CdrData> stream = cdrRepo.streamAllBy(Sort.by("startTime").ascending())) {
            Iterator<CdrData> it = stream.iterator();
            List<CdrData> batch = new ArrayList<>(batchSize);

            while (it.hasNext()) {
                batch.add(it.next());
                if (batch.size() == batchSize || !it.hasNext()) {
                    String csv = serializer.convertToCsv(batch);
                    sendCsvBatch(csv, batch.size());
                    batch.clear();
                }
            }
        }
        log.info("exportCallsData completed");
    }

    /**
     * Отправляет CSV-партию записей в RabbitMQ.
     *
     * @param csv   строка в формате CSV
     * @param count количество записей в партии
     */
    private void sendCsvBatch(String csv, int count) {
        log.info("Sending batch of {} records", count);
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, csv, msg -> {
                msg.getMessageProperties().setContentType("text/csv");
                msg.getMessageProperties().setContentEncoding("UTF-8");
                return msg;
            });
        } catch (Exception e) {
            log.error("Failed to send batch to RabbitMQ", e);
        }
    }

}

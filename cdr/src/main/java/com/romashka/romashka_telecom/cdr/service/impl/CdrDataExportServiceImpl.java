package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.common.config.TimeProperties;

import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.repository.CdrDataRepository;
import com.romashka.romashka_telecom.cdr.service.CdrDataExportService;
import com.romashka.romashka_telecom.cdr.service.CdrDataSerializerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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

    private final TaskScheduler scheduler;
    private final TimeProperties timeProps;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${export.batch.size:10}")
    private int batchSize;

    private long prevDelay = 0;
    private final Object lock = new Object();

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
                if (batch.size() == batchSize) {
                    String csv = serializer.convertToCsv(batch);
                    log.info(csv);
                    scheduleCsvBatch(csv, batch);
//                    sendCsvBatch(csv, batch.size());
//                    batch.clear();
                }
            }
        }
        log.info("exportCallsData completed");
    }

    /**
     * Расчёт задержки на основе времени последней записи в batch и планирование отправки.
     */
    private void scheduleCsvBatch(String csv, List<CdrData> batch) {
        LocalDateTime lastEnd = batch.get(batch.size() - 1).getEndTime();
        // смещение симуляционного времени от начала периода
        Duration simOffset = Duration.between(timeProps.getStart(), lastEnd);
        long delay = (long)(simOffset.toMillis() / timeProps.getCoefficient());

        synchronized (lock) {
            if (delay <= prevDelay) {
                delay = prevDelay + 1; // Гарантируем, что текущий батч отправляется после предыдущего
            }
            prevDelay = delay; // Обновляем значение
        }

        Instant sendAt = Instant.now().plusMillis(delay);
        scheduler.schedule(() -> sendCsvBatch(csv, batch.size(), lastEnd), sendAt);
        log.info("Scheduled CSV batch of {} records at modelTime={} (delay={}ms, coef={})",
                batch.size(), lastEnd, delay, timeProps.getCoefficient());

        batch.clear();
    }

    /**
     * Отправляет CSV-партию записей в RabbitMQ.
     *
     * @param csv   строка в формате CSV
     * @param count количество записей в партии
     */
    private void sendCsvBatch(String csv, int count, LocalDateTime modelTime) {
        log.info("Sending batch of {} records [modelTime={}, actualTime={}]", count, modelTime, LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, csv, msg -> {
                msg.getMessageProperties().setContentType("text/csv");
                msg.getMessageProperties().setHeader("fileName", "cdr_" + modelTime + ".csv");
                msg.getMessageProperties().setContentEncoding("UTF-8");
                return msg;
            });
        } catch (Exception e) {
            log.error("Failed to send batch to RabbitMQ", e);
        }
    }

}

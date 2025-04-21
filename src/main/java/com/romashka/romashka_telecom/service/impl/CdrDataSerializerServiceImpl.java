package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.CdrData;
import com.romashka.romashka_telecom.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CdrDataSerializerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CdrDataSerializerServiceImpl implements CdrDataSerializerService {

    private static final Logger log = LoggerFactory.getLogger(CdrDataSerializerServiceImpl.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CdrDataRepository cdrRepo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${export.batch.size:10}")
    private int batchSize;

    @Autowired
    public CdrDataSerializerServiceImpl(
            CdrDataRepository cdrRepo,
            RabbitTemplate rabbitTemplate
    ) {
        this.cdrRepo = cdrRepo;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public void exportCallsData() {
        log.info("Starting exportCallsData, batchSize={}", batchSize);

        // streamAllBy должен быть объявлен в репозитории как:
        // Stream<CdrData> streamAllBy(Sort sort);
        try (Stream<CdrData> stream = cdrRepo.streamAllBy(Sort.by("startTime").ascending())) {
            Iterator<CdrData> it = stream.iterator();
            List<CdrData> batch = new ArrayList<>(batchSize);

            while (it.hasNext()) {
                batch.add(it.next());
                if (batch.size() == batchSize || !it.hasNext()) {
                    String csv = convertToCsv(batch);
                    sendCsvBatch(csv, batch.size());
                    batch.clear();
                }
            }
        }
        log.info("exportCallsData completed");
    }

    private String convertToCsv(List<CdrData> batch) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Writer w = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            w.write("call_type,caller_number,contact_number,start_time,end_time\n");
            for (CdrData d : batch) {
                w.write(String.format("%s,%s,%s,%s,%s%n",
                        escape(d.getCallType().name()),
                        escape(d.getCallerNumber()),
                        escape(d.getContactNumber()),
                        d.getStartTime().format(DTF),
                        d.getEndTime().format(DTF)
                ));
            }
            w.flush();
            return baos.toString(StandardCharsets.UTF_8.name());

        } catch (IOException ex) {
            throw new UncheckedIOException("CSV conversion failed", ex);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        String r = s.replace("\"", "\"\"");
        return r.contains(",") ? "\"" + r + "\"" : r;
    }

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

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCallsGenerationComplete(CallsGenerationCompletedEvent event) {
        try {
            System.out.printf("Starting export of %d records...%n", event.getGeneratedRecords());
            exportCallsData();
            System.out.println("Export completed successfully");
        } catch (Exception e) {
            System.err.println("Export failed: " + e.getMessage());
            // Можно добавить дополнительную обработку ошибок
        }
    }
}

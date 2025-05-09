package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.service.CdrDataListenerService;
import com.romashka.romashka_telecom.brt.service.CdrDataProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.service.CdrCsvParser;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class CdrDataListenerServiceImpl implements CdrDataListenerService {

    private final CdrCsvParser parser;
    private final CdrDataProcessorService processor;

    // TODO: название очереди должно браться из одного места в разных классах
    private static final String DEFAULT_QUEUE = "cdr.queue";
    @RabbitListener(
            queues = "${rabbitmq.queue.name:" + DEFAULT_QUEUE + "}",
            containerFactory = "cdrListenerContainerFactory"
    )
    public void handleFile(String csv) {
        log.info("Получили CSV-пакет ({} байт)", csv.length());
        List<CdrRecord> records = parser.parse(csv);
        log.info("Распарсили {} записей", records.size());
        processor.process(records);
    }

}

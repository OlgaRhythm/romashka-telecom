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

/**
 * Реализация сервиса для прослушивания и обработки входящих CDR-записей.
 * Получает CSV-данные из очереди RabbitMQ, парсит их и передает на обработку.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdrDataListenerServiceImpl implements CdrDataListenerService {

    private final CdrCsvParser parser;
    private final CdrDataProcessorService processor;
    private static final String DEFAULT_QUEUE = "cdr.queue";

    /**
     * Обрабатывает входящий CSV-файл с CDR-записями.
     * Метод вызывается автоматически при получении сообщения из очереди RabbitMQ.
     *
     * @param csv содержимое CSV-файла с записями о звонках
     */
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

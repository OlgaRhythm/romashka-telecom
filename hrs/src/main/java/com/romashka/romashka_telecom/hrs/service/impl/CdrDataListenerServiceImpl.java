package com.romashka.romashka_telecom.hrs.service.impl;

import com.romashka.romashka_telecom.hrs.service.CdrDataListenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class CdrDataListenerServiceImpl implements CdrDataListenerService {

//    private final CdrCsvParser parser;
//    private final SdrDataProcessorService processor;

    // TODO: название очереди должно браться из одного места в разных классах
//    private static final String DEFAULT_QUEUE = "cdr.queue";
////    @RabbitListener(queues = "${rabbitmq.queue.name:cdr.queue:" + DEFAULT_QUEUE + "}")
////    public void handleFile(String csv) {
////        log.info("Получили CSV-пакет ({} байт)", csv.length());
////        List<CdrRecord> records = parser.parse(csv);
////        log.info("Распарсили {} записей", records.size());
////        processor.process(records);
//    }

}

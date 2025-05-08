package com.romashka.romashka_telecom.brt.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class HrsListenerServiceImpl {

    @RabbitListener(
            queues = "${rabbitmq.hrs.queue.name}",
            containerFactory = "hrsListenerContainerFactory"
    )
    public void handleHrsMessage(/*HrsRequest request*/) {
        // request уже десериализован из JSON
        // ... логика обработки ...
    }
}

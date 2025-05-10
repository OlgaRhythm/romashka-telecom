package com.romashka.romashka_telecom.hrs.service.impl;

import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import com.romashka.romashka_telecom.hrs.service.BrtListenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrtListenerServiceImpl implements BrtListenerService {

    private final BillingProcessorServiceImpl billingProcessorService;

    @RabbitListener(queues = "${rabbitmq.brt-to-hrs.queue.name}")
    public void handleBillingMessage(BillingMessage message) {
        log.info("Получено сообщение от BRT: {}", message);
        billingProcessorService.processBillingMessage(message);
    }
} 

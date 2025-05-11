package com.romashka.romashka_telecom.crm.service;

import com.romashka.romashka_telecom.crm.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqIntegrationService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.crm-to-brt.exchange.name}")
    private String crmToBrtExchange;
    @Value("${rabbitmq.crm-to-brt.routing.key}")
    private String crmToBrtRoutingKey;

    @Value("${rabbitmq.crm-to-hrs.exchange.name}")
    private String crmToHrsExchange;
    @Value("${rabbitmq.crm-to-hrs.routing.key}")
    private String crmToHrsRoutingKey;

    // Отправка сообщения в BRT
    public void sendToBrt(Object message) {
        rabbitTemplate.convertAndSend(crmToBrtExchange, crmToBrtRoutingKey, message);
        log.info("[CRM] Отправлено сообщение в BRT: {}", message);
    }

    // Отправка сообщения в HRS
    public void sendToHrs(Object message) {
        rabbitTemplate.convertAndSend(crmToHrsExchange, crmToHrsRoutingKey, message);
        log.info("[CRM] Отправлено сообщение в HRS: {}", message);
    }

    // Получение сообщений от BRT
    @RabbitListener(queues = "#{'${rabbitmq.brt-to-crm.queue.name}'}")
    public void receiveFromBrt(Object message) {
        log.info("[CRM] Получено сообщение от BRT: {}", message);
        // TODO: Реализовать обработку сообщений от BRT
    }

    // Получение сообщений от HRS
    @RabbitListener(queues = "#{'${rabbitmq.hrs-to-crm.queue.name}'}")
    public void receiveFromHrs(Object message) {
        log.info("[CRM] Получено сообщение от HRS: {}", message);
        // TODO: Реализовать обработку сообщений от HRS
    }
} 
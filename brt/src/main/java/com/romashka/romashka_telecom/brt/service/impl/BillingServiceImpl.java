package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.model.BillingMessage;
import com.romashka.romashka_telecom.brt.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.hrs.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.hrs.routing.key}")
    private String routingKey;

    @Override
    public void processAndSendBillingData(BillingMessage message) {
        try {
            log.info("Sending billing message for callId={}, callerId={}", 
                    message.getCallId(), message.getCallerId());
            
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            
            log.info("Successfully sent billing message");
        } catch (Exception e) {
            log.error("Failed to send billing message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send billing message", e);
        }
    }

    @Override
    public void chargeMonthlyFee(LocalDate modelDate) {
        log.info("⏰ [BillingService] Инициирована проверка и списание абонплаты за модельный день: {}", modelDate);
    }
}
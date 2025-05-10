package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.model.BillingMessage;
import com.romashka.romashka_telecom.brt.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.LocalDate;

@Slf4j
@Service
public class BillingServiceImpl implements BillingService {
    @Qualifier("hrsRabbitTemplate")
    @Autowired
    private final RabbitTemplate hrsRabbitTemplate;

    @Value("${rabbitmq.brt-to-hrs.exchange.name}")
    private String brtToHrsExchangeName;

    @Value("${rabbitmq.brt-to-hrs.routing.key}")
    private String brtToHrsRoutingKey;

    public BillingServiceImpl(
            @Qualifier("hrsRabbitTemplate") RabbitTemplate hrsRabbitTemplate
    ) {
        this.hrsRabbitTemplate = hrsRabbitTemplate;
    }

    @Override
    public void processAndSendBillingData(BillingMessage message) {
        try {
            log.info("Отправка сообщения в HRS: {}", message);
            hrsRabbitTemplate.convertAndSend(brtToHrsExchangeName, brtToHrsRoutingKey, message);
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
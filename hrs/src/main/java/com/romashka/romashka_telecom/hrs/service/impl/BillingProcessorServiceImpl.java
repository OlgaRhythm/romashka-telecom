package com.romashka.romashka_telecom.hrs.service.impl;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import com.romashka.romashka_telecom.hrs.model.HrsResponse;
import com.romashka.romashka_telecom.hrs.repository.CallCostRepository;
import com.romashka.romashka_telecom.hrs.repository.RateRepository;
import com.romashka.romashka_telecom.hrs.service.tariff.TariffStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingProcessorServiceImpl {

    private final CallCostRepository callCostRepository;
    private final RateRepository rateRepository;
    private final TariffStrategyFactory tariffStrategyFactory;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.hrs-to-brt.exchange.name}")
    private String hrsToBrtExchangeName;

    @Value("${rabbitmq.hrs-to-brt.routing.key}")
    private String hrsToBrtRoutingKey;

    @Transactional
    public void processBillingMessage(BillingMessage message) {
        log.info("Начало обработки сообщения от BRT: {}", message);

        if (message.getResources() == null) {
            message.setResources(new java.util.HashMap<>());
        }

        // 1. Получаем тариф
        Rate rate = rateRepository.findById(message.getRateId())
                .orElseThrow(() -> new RuntimeException("Тариф не найден: " + message.getRateId()));

        // 2. Получаем стоимость звонка
        CallCost callCost = callCostRepository.findByCallTypeAndNetworkType(
                message.getCallType(),
                message.getNetworkType()
        ).orElseGet(() -> {
            CallCost empty = new CallCost();
            empty.setCallCost(BigDecimal.ZERO);
            return empty;
        });

        // 3. Получаем стратегию расчета для тарифа и рассчитываем стоимость
        Map<String, BigDecimal> resources = tariffStrategyFactory.getStrategy(rate)
                .calculateCost(message, rate, callCost);

        // 4. Формируем ответное сообщение
        HrsResponse response = HrsResponse.builder()
                .callerId(message.getCallerId())
                .resources(resources.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().doubleValue()
                        )))
                .build();

        // 5. Отправляем ответ в BRT
        log.info("Отправка ответа в BRT: {}", response);
        rabbitTemplate.convertAndSend(hrsToBrtExchangeName, hrsToBrtRoutingKey, response);
    }
} 
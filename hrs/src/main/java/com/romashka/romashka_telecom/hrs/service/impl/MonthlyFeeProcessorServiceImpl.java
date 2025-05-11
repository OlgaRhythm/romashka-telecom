package com.romashka.romashka_telecom.hrs.service.impl;

import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.MonthlyFeeRequest;
import com.romashka.romashka_telecom.hrs.model.MonthlyFeeResponse;
import com.romashka.romashka_telecom.hrs.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyFeeProcessorServiceImpl {

    private final RateRepository rateRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.exchange.name}")
    private String monthlyFeeResponseExchangeName;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.routing.key}")
    private String monthlyFeeResponseRoutingKey;

    @RabbitListener(queues = "${rabbitmq.monthly-fee-brt-to-hrs.queue.name}")
    @Transactional
    public void processMonthlyFeeRequest(MonthlyFeeRequest request) {
        log.info("Получен запрос на проверку абонплаты: {}", request);

        Rate rate = rateRepository.findById(request.getRateId())
                .orElseThrow(() -> new RuntimeException("Тариф не найден: " + request.getRateId()));

        BigDecimal feeAmount = BigDecimal.ZERO;
        Map<String, Double> resources = new HashMap<>();
        
        // Проверяем, есть ли у тарифа период оплаты
        if (rate.getPeriodDuration() != null && rate.getPeriodDuration() > 0) {
            // Если количество дней с момента активации кратно периоду оплаты
            int daysSinceActivation = request.getDaysSinceActivation();
            if (daysSinceActivation != 0 && daysSinceActivation % rate.getPeriodDuration() == 0) {
                feeAmount = rate.getPeriodPrice();
                
                // Добавляем ресурсы, если они предусмотрены тарифом
                if (rate.getAddedMinutes() != null && rate.getAddedMinutes() > 0) {
                    resources.put("minutes", rate.getAddedMinutes().doubleValue());
                }
            }
        }

        MonthlyFeeResponse response = MonthlyFeeResponse.builder()
                .callerId(request.getCallerId())
                .feeAmount(feeAmount)
                .resources(resources)
                .build();

        log.info("Отправка ответа на запрос абонплаты: {}", response);
        rabbitTemplate.convertAndSend(monthlyFeeResponseExchangeName, monthlyFeeResponseRoutingKey, response);
    }
} 
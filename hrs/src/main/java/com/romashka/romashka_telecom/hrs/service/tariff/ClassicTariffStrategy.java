package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ClassicTariffStrategy implements TariffCalculationStrategy {

    @Override
    public Map<String, BigDecimal> calculateCost(BillingMessage message, Rate rate, CallCost callCost) {
        log.info("Расчет стоимости для тарифа 'Классика':");
        log.info("- Тип звонка: {}", message.getCallType());
        log.info("- Тип сети: {}", message.getNetworkType());
        log.info("- Стоимость минуты: {}", callCost.getCallCost());
        log.info("- Длительность звонка: {} минут", message.getDurationMinutes());

        Map<String, BigDecimal> resources = new HashMap<>();
        
        // Рассчитываем стоимость звонка на основе данных из call_cost
        BigDecimal totalCost = callCost.getCallCost()
                .multiply(BigDecimal.valueOf(message.getDurationMinutes()));
        
        log.info("- Итоговая стоимость: {}", totalCost);
        
        resources.put("money", totalCost);
        
        return resources;
    }
} 
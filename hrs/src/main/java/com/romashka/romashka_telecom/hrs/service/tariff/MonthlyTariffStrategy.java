package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyTariffStrategy implements TariffCalculationStrategy {

    @Override
    public Map<String, BigDecimal> calculateCost(BillingMessage message, Rate rate, CallCost callCost) {
        log.info("Расчет стоимости для тарифа 'Помесячный':");
        log.info("- Тип звонка: {}", message.getCallType());
        log.info("- Тип сети: {}", message.getNetworkType());
        log.info("- Длительность звонка: {} минут", message.getDurationMinutes());
        log.info("- Доступных минут: {}", message.getResources().get("minutes"));

        Map<String, BigDecimal> resources = new HashMap<>();
        Long availableMinutes = message.getResources().get("minutes").longValue();
        
        // Если достаточно доступных минут
        if (message.getDurationMinutes() <= availableMinutes) {
            log.info("- Звонок в пределах доступных минут");
            resources.put("minutes", BigDecimal.valueOf(message.getDurationMinutes()));
            return resources;
        }

        // Если звонок превышает доступные минуты
        Long paidMinutes = message.getDurationMinutes() - availableMinutes;
        BigDecimal additionalCost = callCost.getCallCost()
                .multiply(BigDecimal.valueOf(paidMinutes));

        log.info("- Оплачиваемых минут: {}", paidMinutes);
        log.info("- Дополнительная стоимость: {}", additionalCost);

        resources.put("money", additionalCost);
        resources.put("minutes", BigDecimal.valueOf(availableMinutes));
        
        return resources;
    }
} 
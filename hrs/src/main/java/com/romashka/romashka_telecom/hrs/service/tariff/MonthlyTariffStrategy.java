package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import com.romashka.romashka_telecom.hrs.repository.CallCostRepository;
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
    private final CallCostRepository callCostRepository;

    @Override
    public Map<String, BigDecimal> calculateCost(BillingMessage message, Rate rate) {
        // 2. Получаем стоимость звонка
        CallCost callCostClassic;
        try {
            callCostClassic = callCostRepository.findByRateIdAndCallTypeAndNetworkType(
                    11L,
                    message.getCallType(),
                    message.getNetworkType()
            );
        } catch (Exception e) {
            log.warn("Исключение при получении стоимости звонка: ", e);
            callCostClassic = new CallCost();
            callCostClassic.setCallCost(BigDecimal.ZERO);
        }

        if (callCostClassic == null) {
            log.warn("Стоимость звонка не найдена, используем нулевую стоимость");
            callCostClassic = new CallCost();
            callCostClassic.setCallCost(BigDecimal.ZERO);
        }
        
        log.info("Расчет стоимости для тарифа 'Помесячный':");
        log.info("- Тип звонка: {}", message.getCallType());
        log.info("- Тип сети: {}", message.getNetworkType());
        log.info("- Длительность звонка: {} минут", message.getDurationMinutes());
        
        if (message.getResources() == null || !message.getResources().containsKey("minutes")) {
            log.warn("Ресурсы не найдены или отсутствуют минуты, используем нулевое значение");
            message.setResources(new HashMap<>());
            message.getResources().put("minutes", 0.0);
        }
        
        log.info("- Доступных минут: {}", message.getResources().get("minutes"));

        Map<String, BigDecimal> resources = new HashMap<>();
        Long availableMinutes = message.getResources().get("minutes").longValue();
        
        // Если достаточно доступных минут
        if (message.getDurationMinutes() <= availableMinutes) {
            log.info("- Звонок в пределах доступных минут");
            resources.put("minutes", BigDecimal.valueOf(message.getDurationMinutes()));
            return resources;
        }

        // Если недостаточно доступных минут
        if (availableMinutes > 0) {
            Long paidMinutes = message.getDurationMinutes() - availableMinutes;
            BigDecimal additionalCost = callCostClassic.getCallCost()
                    .multiply(BigDecimal.valueOf(paidMinutes));

            log.info("- Списание минут: {}", availableMinutes);       
            log.info("- Оплачиваемых минут: {}", paidMinutes);
            log.info("- Дополнительная стоимость: {}", additionalCost);

            resources.put("money", additionalCost);
            resources.put("minutes", BigDecimal.valueOf(availableMinutes));

            return resources;
        }

        // Если на балансе не осталось минут
        BigDecimal additionalCost = callCostClassic.getCallCost()
                .multiply(BigDecimal.valueOf(message.getDurationMinutes()));

        log.info("- Оплата по тарифу 'Классика'");
        log.info("- Стоимость: {}", additionalCost);

        resources.put("money", additionalCost);

        return resources;
    }
} 
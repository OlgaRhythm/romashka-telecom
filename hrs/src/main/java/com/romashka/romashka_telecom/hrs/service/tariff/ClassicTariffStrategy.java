package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import com.romashka.romashka_telecom.hrs.repository.CallCostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicTariffStrategy implements TariffCalculationStrategy {
    private final CallCostRepository callCostRepository;

    @Override
    public Map<String, BigDecimal> calculateCost(BillingMessage message, Rate rate) {

        // 2. Получаем стоимость звонка
        CallCost callCost;
        try {
            callCost = callCostRepository.findByRateIdAndCallTypeAndNetworkType(
                    message.getRateId(),
                    message.getCallType(),
                    message.getNetworkType()
            );
        } catch (Exception e) {
            log.warn("Исключение при получении стоимости звонка: ", e);
            callCost = new CallCost();
            callCost.setCallCost(BigDecimal.ZERO);
        }

        if (callCost == null) {
            log.warn("Стоимость звонка не найдена, используем нулевую стоимость");
            callCost = new CallCost();
            callCost.setCallCost(BigDecimal.ZERO);
        }


        log.info("Расчет стоимости для тарифа 'Классика':");
        log.info("- Тип звонка: {}", message.getCallType());
        log.info("- Тип сети: {}", message.getNetworkType());        
        log.info("- Стоимость минуты: {}", callCost.getCallCost());
        log.info("- Длительность звонка: {} минут", message.getDurationMinutes());

        Map<String, BigDecimal> resources = new HashMap<>();
        
        // Рассчитываем стоимость звонка на основе данных из call_cost
        BigDecimal totalCost = callCost.getCallCost() != null ? 
            callCost.getCallCost().multiply(BigDecimal.valueOf(message.getDurationMinutes())) :
            BigDecimal.ZERO;
        
        log.info("- Итоговая стоимость: {}", totalCost);
        
        resources.put("money", totalCost);
        
        return resources;
    }
} 
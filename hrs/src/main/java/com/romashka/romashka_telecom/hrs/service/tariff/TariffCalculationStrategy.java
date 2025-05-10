package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;

import java.math.BigDecimal;
import java.util.Map;

public interface TariffCalculationStrategy {
    /**
     * Рассчитывает стоимость звонка в соответствии с тарифом
     * @param message сообщение с информацией о звонке
     * @param rate тариф
     * @param callCost стоимость звонка
     * @return карта ресурсов с суммами списания (ключ - название ресурса, значение - сумма)
     */
    Map<String, BigDecimal> calculateCost(BillingMessage message, Rate rate, CallCost callCost);
} 
package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.Rate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TariffStrategyFactory {
    private final ClassicTariffStrategy classicTariffStrategy;
    private final MonthlyTariffStrategy monthlyTariffStrategy;

    public TariffCalculationStrategy getStrategy(Rate rate) {
        return switch (rate.getRateType()) {
            case CLASSIC -> classicTariffStrategy;
            case MONTHLY -> monthlyTariffStrategy;
            default -> throw new IllegalArgumentException("Неизвестный тип тарифа: " + rate.getRateType());
        };
    }
} 
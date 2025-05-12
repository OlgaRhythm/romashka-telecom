package com.romashka.romashka_telecom.hrs.service.tariff;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.entity.Rate;
import com.romashka.romashka_telecom.hrs.enums.CallType;
import com.romashka.romashka_telecom.hrs.enums.NetworkType;
import com.romashka.romashka_telecom.hrs.model.BillingMessage;
import com.romashka.romashka_telecom.hrs.repository.CallCostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TariffStrategiesTest {

    private CallCostRepository callCostRepository;
    private ClassicTariffStrategy classicStrategy;
    private MonthlyTariffStrategy monthlyStrategy;

    @BeforeEach
    void setUp() {
        callCostRepository = Mockito.mock(CallCostRepository.class);
        classicStrategy = new ClassicTariffStrategy(callCostRepository);
        monthlyStrategy = new MonthlyTariffStrategy(callCostRepository);
    }

    @Test
    void classicTariffStrategy_ShouldCalculateCost() {
        // Arrange
        Rate rate = new Rate();
        rate.setRateId(1L);

        CallCost callCost = new CallCost();
        callCost.setCallCost(new BigDecimal("2.50"));

        BillingMessage message = BillingMessage.builder()
                .rateId(1L)
                .callType(CallType.OUTGOING)
                .networkType(NetworkType.EXTERNAL)
                .durationMinutes(4L)
                .build();

        Mockito.when(callCostRepository.findByRateIdAndCallTypeAndNetworkType(
                1L, CallType.OUTGOING, NetworkType.EXTERNAL
        )).thenReturn(callCost);

        // Act
        Map<String, BigDecimal> result = classicStrategy.calculateCost(message, rate);

        // Assert
        assertEquals(new BigDecimal("10.00"), result.get("money"));
    }

    @Test
    void monthlyTariffStrategy_ShouldUseAvailableMinutes() {
        // Arrange
        Rate rate = new Rate();
        rate.setRateId(1L);

        CallCost callCost = new CallCost();
        callCost.setCallCost(new BigDecimal("1.00"));

        Map<String, Double> resources = new HashMap<>();
        resources.put("minutes", 10.0);

        BillingMessage message = BillingMessage.builder()
                .rateId(1L)
                .callType(CallType.OUTGOING)
                .networkType(NetworkType.EXTERNAL)
                .durationMinutes(5L)
                .resources(resources)
                .build();

        Mockito.when(callCostRepository.findByRateIdAndCallTypeAndNetworkType(
                11L, CallType.OUTGOING, NetworkType.EXTERNAL
        )).thenReturn(callCost);

        // Act
        Map<String, BigDecimal> result = monthlyStrategy.calculateCost(message, rate);

        // Assert
        assertEquals(BigDecimal.valueOf(5L), result.get("minutes"));
        assertNull(result.get("money"));
    }

    @Test
    void monthlyTariffStrategy_ShouldChargeMoneyIfNotEnoughMinutes() {
        // Arrange
        Rate rate = new Rate();
        rate.setRateId(1L);

        CallCost callCost = new CallCost();
        callCost.setCallCost(new BigDecimal("1.00"));

        Map<String, Double> resources = new HashMap<>();
        resources.put("minutes", 3.0);

        BillingMessage message = BillingMessage.builder()
                .rateId(1L)
                .callType(CallType.OUTGOING)
                .networkType(NetworkType.EXTERNAL)
                .durationMinutes(5L)
                .resources(resources)
                .build();

        Mockito.when(callCostRepository.findByRateIdAndCallTypeAndNetworkType(
                11L, CallType.OUTGOING, NetworkType.EXTERNAL
        )).thenReturn(callCost);

        // Act
        Map<String, BigDecimal> result = monthlyStrategy.calculateCost(message, rate);

        // Assert
        assertEquals(BigDecimal.valueOf(3L), result.get("minutes"));
        assertEquals(0, result.get("money").compareTo(BigDecimal.valueOf(2L)));
    }
} 
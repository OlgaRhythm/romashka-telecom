package com.romashka.romashka_telecom.brt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFeeResponse {
    private Long callerId;
    private BigDecimal feeAmount;
    private Map<String, Double> resources; // ключ - название ресурса (минуты), значение - сумма добавления
} 
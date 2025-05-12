package com.romashka.romashka_telecom.brt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

import java.math.BigDecimal;

/**
 * Ответ от HRS-сервиса с результатом расчета абонентской платы.
 * Содержит информацию о сумме к оплате и дополнительных ресурсах, которые будут начислены абоненту.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFeeResponse {
    /**
     * Идентификатор абонента, для которого был выполнен расчет.
     */
    private Long callerId;

    /**
     * Сумма абонентской платы к оплате.
     */
    private BigDecimal feeAmount;

    /**
     * Карта дополнительных ресурсов, которые будут начислены абоненту.
     * Ключ - название ресурса (например, "минуты"),
     * Значение - количество ресурса, которое будет добавлено к текущему балансу.
     */
    private Map<String, Double> resources;
} 
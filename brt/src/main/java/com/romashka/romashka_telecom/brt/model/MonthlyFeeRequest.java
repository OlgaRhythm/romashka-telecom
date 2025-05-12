package com.romashka.romashka_telecom.brt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на расчет абонентской платы.
 * Используется для отправки в HRS-сервис для расчета стоимости абонентской платы.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFeeRequest {
    /**
     * Идентификатор абонента, для которого рассчитывается абонентская плата.
     */
    private Long callerId;

    /**
     * Идентификатор тарифного плана абонента.
     */
    private Long rateId;

    /**
     * Количество дней с момента активации тарифа.
     * Используется для расчета пропорциональной абонентской платы.
     */
    private Integer daysSinceActivation;
} 
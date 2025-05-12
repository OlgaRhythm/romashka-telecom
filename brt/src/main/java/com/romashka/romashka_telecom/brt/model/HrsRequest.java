package com.romashka.romashka_telecom.brt.model;

import lombok.Data;
import java.util.Map;

/**
 * Запрос к HRS-сервису для расчета стоимости звонка.
 * Содержит информацию о звонке и текущих ресурсах абонента.
 */
@Data
public class HrsRequest {
    /**
     * Идентификатор звонка в системе.
     */
    private Long callId;

    /**
     * Идентификатор абонента, совершившего звонок.
     */
    private Long callerId;

    /**
     * Карта ресурсов абонента, которые будут использованы для оплаты звонка.
     * Ключ - название ресурса (например, "деньги", "минуты"),
     * Значение - количество ресурса, которое будет списано.
     */
    private Map<String, Double> resources;
} 
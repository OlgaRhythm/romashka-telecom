package com.romashka.romashka_telecom.brt.model;

import com.romashka.romashka_telecom.brt.enums.CallType;
import com.romashka.romashka_telecom.brt.enums.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Сообщение для биллинга, содержащее информацию о звонке.
 * Используется для передачи данных между компонентами системы биллинга.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingMessage {
    /**
     * Идентификатор абонента, совершившего звонок.
     */
    private Long callerId;
//    private Long callId;
    /**
     * Идентификатор тарифного плана абонента.
     */
    private Long rateId;
    /**
     * Продолжительность звонка в минутах.
     */
    private Long durationMinutes;
    /**
     * Тип звонка (входящий или исходящий).
     */
    private CallType callType; // входящий или исходящий
    /**
     * Тип сети (внутри сети или на другую сеть).
     */
    private NetworkType networkType; // внутри сети или на другую сеть
    /**
     * Карта ресурсов абонента, которые будут использованы для оплаты звонка.
     * Ключ - название ресурса (например, "деньги", "минуты"),
     * Значение - количество ресурса, которое будет списано.
     */
    private Map<String, Double> resources = new HashMap<>();
} 
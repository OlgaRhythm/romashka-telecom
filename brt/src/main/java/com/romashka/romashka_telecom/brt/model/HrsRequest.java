package com.romashka.romashka_telecom.brt.model;

import lombok.Data;
import java.util.Map;

@Data
public class HrsRequest {
    private Long callId;
    private Long callerId;
    private Map<String, Double> resources; // ключ - название ресурса (деньги, минуты), значение - сумма списания
} 
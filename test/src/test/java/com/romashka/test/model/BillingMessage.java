package com.romashka.test.model;

import com.romashka.romashka_telecom.hrs.enums.CallType;
import com.romashka.romashka_telecom.hrs.enums.NetworkType;
import lombok.Data;
import java.util.Map;

@Data
public class BillingMessage {
    private Long callerId;
    private Long rateId;
    private Long durationMinutes;
    private CallType callType; // входящий или исходящий
    private NetworkType networkType; // внутри сети или на другую сеть
    private Map<String, Double> resources;
} 
package com.romashka.romashka_telecom.hrs.model;

import com.romashka.romashka_telecom.hrs.enums.CallType;
import com.romashka.romashka_telecom.hrs.enums.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingMessage {
    private Long callerId;
    private Long rateId;
    private Long durationMinutes;
    private CallType callType; // входящий или исходящий
    private NetworkType networkType; // внутри сети или на другую сеть
    private Map<String, Double> resources = new HashMap<>();
} 
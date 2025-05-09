package com.romashka.romashka_telecom.brt.model;

import com.romashka.romashka_telecom.brt.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingMessage {
    private Long callerId;
    private Long callId;
    private Long rateId;
    private Long durationMinutes;
    private CallType callType;
    private Map<String, Double> resources;
} 
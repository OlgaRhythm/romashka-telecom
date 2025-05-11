package com.romashka.romashka_telecom.crm.model;

import lombok.Data;
import java.time.Instant;

@Data
public class PaymentResponse {
    private Long transactionId;
    private String msisdn;
    private Double amount;
    private Double newBalance;
    private Instant transactionTime;
} 
package com.romashka.romashka_telecom.crm.model;

import lombok.Data;

@Data
public class PaymentRequest {
    private String msisdn;
    private Double amount;
} 
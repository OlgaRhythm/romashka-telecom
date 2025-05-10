package com.romashka.romashka_telecom.crm.model;

import lombok.Data;

@Data
public class AddSubscriberResponse {
    private Long subscriberId;
    private String subscriberName;
    private String msisdn;
    private Integer tariffId;
    private Double balance = 100.0; // По умолчанию 100 у.е.
} 
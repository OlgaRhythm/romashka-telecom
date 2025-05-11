package com.romashka.romashka_telecom.crm.model;

import lombok.Data;

@Data
public class AddSubscriberRequest {
    private String subscriberName;
    private String msisdn;
    private Integer tariffId = 11; // По умолчанию тариф "Классика"
} 
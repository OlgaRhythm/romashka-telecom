package com.romashka.romashka_telecom.crm.model;

import lombok.Data;

@Data
public class ChangeTariffRequest {
    private String msisdn;
    private Integer tariffId;
} 
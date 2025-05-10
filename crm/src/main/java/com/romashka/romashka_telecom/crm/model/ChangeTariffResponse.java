package com.romashka.romashka_telecom.crm.model;

import lombok.Data;
import java.time.Instant;

@Data
public class ChangeTariffResponse {
    private String msisdn;
    private Integer tariffId;
    private Instant tariffDate;
} 
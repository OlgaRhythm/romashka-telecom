package com.romashka.romashka_telecom.hrs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFeeRequest {
    private Long callerId;
    private Long rateId;
    private Integer daysSinceActivation;
} 
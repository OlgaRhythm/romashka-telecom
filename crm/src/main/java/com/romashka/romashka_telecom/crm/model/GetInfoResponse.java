package com.romashka.romashka_telecom.crm.model;

import lombok.Data;

@Data
public class GetInfoResponse {
    private SubscriberInfo subscriber;
    private TariffInfo tariff;

    @Data
    public static class SubscriberInfo {
        private String fullName;
        private Double balance;
    }

    @Data
    public static class TariffInfo {
        private String tariffName;
        private String tariffType;
        private Integer periodDuration;
        private Integer periodPrice;
        private TariffParameters parameters;
    }

    @Data
    public static class TariffParameters {
        private Integer minutes;
    }
} 
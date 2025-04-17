package com.romashka.romashka_telecom.enums;

import java.util.Arrays;

public enum CallType {
    OUTGOING("01"),
    INCOMING("02");

    private final String code;

    CallType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CallType fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown code: " + code));
    }
}

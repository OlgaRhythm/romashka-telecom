package com.romashka.romashka_telecom.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CallTypeConverter implements AttributeConverter<CallType, String> {

    @Override
    public String convertToDatabaseColumn(CallType callType) {
        if (callType == null) {
            return null;
        }
        return callType.getCode();
    }

    @Override
    public CallType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return CallType.fromCode(code);
    }
}
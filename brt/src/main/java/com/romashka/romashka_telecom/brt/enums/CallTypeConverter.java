package com.romashka.romashka_telecom.brt.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Конвертер для преобразования типов звонков {@link CallType} в строку и наоборот.
 * Используется для преобразования значений типа звонка в строковый код при сохранении в базе данных,
 * и преобразования строкового кода обратно в тип {@link CallType} при извлечении из базы данных.
 */
@Converter(autoApply = true)
public class CallTypeConverter implements AttributeConverter<CallType, String> {

    /**
     * Преобразует объект {@link CallType} в строку для сохранения в базе данных.
     *
     * @param callType Объект {@link CallType}, который необходимо преобразовать.
     * @return Строковый код типа звонка или {@code null}, если тип звонка равен {@code null}.
     */
    @Override
    public String convertToDatabaseColumn(CallType callType) {
        return callType != null ? callType.getCode() : null;
    }

    /**
     * Преобразует строковый код типа звонка обратно в объект {@link CallType}.
     *
     * @param code Строковый код типа звонка.
     * @return Объект {@link CallType}, соответствующий переданному коду, или {@code null}, если код равен {@code null}.
     */
    @Override
    public CallType convertToEntityAttribute(String code) {
        return code != null ? CallType.fromCode(code) : null;
    }
}
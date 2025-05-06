package com.romashka.romashka_telecom.cdr.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для {@link CallTypeConverter}.
 */
public class CallTypeConverterTest {

    private final CallTypeConverter converter = new CallTypeConverter();

    @Test
    void testConvertToDatabaseColumn() {
        // Проверка преобразования из CallType в строку
        assertEquals("01", converter.convertToDatabaseColumn(CallType.OUTGOING));
        assertEquals("02", converter.convertToDatabaseColumn(CallType.INCOMING));
        assertNull(converter.convertToDatabaseColumn(null));  // Проверка на null
    }

    @Test
    void testConvertToEntityAttribute() {
        // Проверка преобразования из строки в CallType
        assertEquals(CallType.OUTGOING, converter.convertToEntityAttribute("01"));
        assertEquals(CallType.INCOMING, converter.convertToEntityAttribute("02"));
        assertNull(converter.convertToEntityAttribute(null));  // Проверка на null
    }

    @Test
    void testInvalidCode() {
        // Проверка на ошибку при некорректном коде
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalidCode"));
    }
}

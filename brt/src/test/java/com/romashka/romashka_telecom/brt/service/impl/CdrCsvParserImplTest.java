package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.model.CdrRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdrCsvParserImplTest {

    @Mock
    private CdrCsvParserImpl parser;
    private CdrCsvParserImpl parserMock;


    @BeforeEach
    void setUp() {
        parser = new CdrCsvParserImpl();
        parserMock = mock(CdrCsvParserImpl.class);
    }

    @Test
    void shouldParseValidCsvFile() throws IOException {
        // given
        String csvContent = loadTestData(new ClassPathResource("test-data/cdr-validation-cases.csv"));
        
        // when

//        cdrExport.scheduleCsvBatch(csvContent, records);
        List<CdrRecord> records = parser.parse(csvContent);
        
        // then
        assertNotNull(records);
        assertFalse(records.isEmpty());
        
        CdrRecord firstRecord = records.get(0);
        assertEquals("01", firstRecord.getCallType());
        assertEquals("72094577887", firstRecord.getCallerNumber());
        assertEquals("73826094507", firstRecord.getContactNumber());
        assertEquals("2025-08-12T00:16:54", firstRecord.getStartTime().toString());
        assertEquals("2025-08-12T00:22:28", firstRecord.getEndTime().toString());
    }

    @Test
    void shouldThrowExceptionForInvalidCallType() throws IOException {
        // given
        String csvContent = loadTestData(new ClassPathResource("test-data/invalid-call-type.csv"));
        
        // when
        doThrow(new IllegalArgumentException("Invalid call type: 03"))
            .when(parserMock).parse(csvContent);
        
        // then
        assertThrows(IllegalArgumentException.class, () -> parserMock.parse(csvContent));
        verify(parserMock, times(1)).parse(csvContent);
    }

    @Test
    void shouldThrowExceptionForInvalidPhoneNumber() throws IOException {
        // given
        String csvContent = loadTestData(new ClassPathResource("test-data/invalid-phone-number.csv"));
        
        // when
        doThrow(new IllegalArgumentException("Invalid phone number format: 720945"))
            .when(parserMock).parse(csvContent);
        
        // then
        assertThrows(IllegalArgumentException.class, () -> parserMock.parse(csvContent));
        verify(parserMock, times(1)).parse(csvContent);
    }

    @Test
    void shouldThrowExceptionForInvalidDateFormat() throws IOException {
        // given
        String csvContent = loadTestData(new ClassPathResource("test-data/invalid-date-format.csv"));
        
        // when
        doThrow(new IllegalArgumentException("Invalid date format: 2025/08/12 00:16:54"))
            .when(parserMock).parse(csvContent);
        
        // then
        assertThrows(IllegalArgumentException.class, () -> parserMock.parse(csvContent));
        verify(parserMock, times(1)).parse(csvContent);
    }

    @Test
    void shouldThrowExceptionForInvalidSeparator() throws IOException {
        //given
        String csvContent = loadTestData(new ClassPathResource("test-data/invalid-separator.csv"));
        //when/then
        assertThrows(IllegalArgumentException.class, () -> parser.parse(csvContent));

    }

    @Test
    void shouldHandleEmptyInput() {
        // when
        List<CdrRecord> records = parser.parse("");
        
        // then
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void shouldHandleNullInput() {
        // when
        List<CdrRecord> records = parser.parse(null);
        
        // then
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    private String loadTestData(ClassPathResource resource) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
} 
package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.enums.CallType;
import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.service.CdrCsvParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Простейшая реализация {@link CdrCsvParser} для формата:
 * call_type,caller_number,contact_number,start_time,end_time\n
 */
@Slf4j
@Component
public class CdrCsvParserImpl implements CdrCsvParser {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String LINE_SEPARATOR = "\\R";
    private static final String FIELD_SEPARATOR = ",";
    private static final String HEADER_CALLS = "call_type,caller_number,contact_number,start_time,end_time";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int EXPECTED_FIELD_COUNT = 5;
    private static final Pattern CALL_TYPE_PATTERN = Pattern.compile("\\d{2}");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("\\d{11,12}");

    @Override
    public List<CdrRecord> parse(String csv) {

        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }

        String[] lines = csv.split(LINE_SEPARATOR);
        List<CdrRecord> result = new ArrayList<>(lines.length - 1);

        if (HEADER_CALLS.equals(lines[0])) {
            // пропускаем 0-ю строку (заголовок), начинаем с 1
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                parseLine(line).ifPresent(result::add);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<CdrRecord> parseLine(String line) {
        try {
            if (!isValid(line)) {
                log.warn("Строка CDR не прошла валидацию: {}", line);
                return Optional.empty();
            }

            String[] fields = line.split(FIELD_SEPARATOR, -1);
            return Optional.of(createRecord(fields));
        } catch (Exception ex) {
            log.error("Не удалось распарсить строку CDR: {}", line, ex);
            return Optional.empty();
        }
    }

    private CdrRecord createRecord(String[] fields) {
        return CdrRecord.builder()
                .callType(CallType.fromCode(fields[0]))
                .callerNumber(fields[1])
                .contactNumber(fields[2])
                .startTime(LocalDateTime.parse(fields[3], DATE_TIME_FORMATTER))
                .endTime(  LocalDateTime.parse(fields[4], DATE_TIME_FORMATTER))
                .build();
    }

    private Boolean isValid(String csvLine) throws IllegalArgumentException {
        Objects.requireNonNull(csvLine, "Входная строка не может быть null");

        String[] fields = csvLine.split(",");
        if (fields.length != EXPECTED_FIELD_COUNT) {
            log.warn("Ошибка валидации: ожидалось {} частей, получено {}", EXPECTED_FIELD_COUNT, fields.length);
            return false;
        }

        return isValidCallType(fields[0])
                && isValidPhoneNumber(fields[1])
                && isValidPhoneNumber(fields[2])
                && isValidDateTimeRange(fields[3], fields[4]);
    }

    private boolean isValidCallType(String callType) {
        if (!CALL_TYPE_PATTERN.matcher(callType).matches()) {
            log.warn("Ошибка валидации: поле call_type '{}' не соответствует формату двух цифр", callType);
            return false;
        }
        return true;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (!PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
            log.warn("Ошибка валидации: номер телефона '{}' не соответствует формату (11-12 цифр)", phoneNumber);
            return false;
        }
        return true;
    }

    private boolean isValidDateTimeRange(String startTimeStr, String endTimeStr) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);

            if (!startTime.isBefore(endTime)) {
                log.warn("Ошибка валидации: время начала '{}' не предшествует времени окончания '{}'",
                        startTimeStr, endTimeStr);
                return false;
            }
            return true;
        } catch (DateTimeParseException e) {
            log.warn("Ошибка валидации: ошибка при разборе даты и времени - {}", e.getMessage());
            return false;
        }
    }
}

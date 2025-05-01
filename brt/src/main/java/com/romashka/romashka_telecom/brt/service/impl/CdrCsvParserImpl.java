package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.service.CdrCsvParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Простейшая реализация {@link CdrCsvParser} для формата:
 * call_type,caller_number,contact_number,start_time,end_time\n
 */
@Slf4j
@Component
public class CdrCsvParserImpl implements CdrCsvParser {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String LINE_SEPARATOR = "\n";
    private static final String FIELD_SEPARATOR = ",";

    @Override
    public List<CdrRecord> parse(String csv) {
        // TODO: проверять правильность файла
        List<CdrRecord> result = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return result;
        }
        String[] lines = csv.split(LINE_SEPARATOR);
        // предполагаем, что первая строка — заголовок
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(FIELD_SEPARATOR, -1);
            if (cols.length < 5) {
                throw new IllegalArgumentException("Неверное количество колонок в CDR: " + line);
            }
            try {
                CdrRecord rec = new CdrRecord(
                        cols[0],
                        cols[1],
                        cols[2],
                        LocalDateTime.parse(cols[3], DTF),
                        LocalDateTime.parse(cols[4], DTF)
                );
                result.add(rec);
            } catch (Exception ex) {
                log.error("Не удалось распарсить строку CDR: {}", line, ex);
                throw new IllegalArgumentException("Ошибка парсинга CDR: " + line, ex);
            }
        }
        return result;
    }
}

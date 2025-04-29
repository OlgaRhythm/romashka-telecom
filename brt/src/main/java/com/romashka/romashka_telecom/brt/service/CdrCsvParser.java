package com.romashka.romashka_telecom.brt.service;

import com.romashka.romashka_telecom.brt.model.CdrRecord;

import java.util.List;

/**
 * Сервис парсинга CSV-пакетов из очереди в список {@link CdrRecord}.
 */
public interface CdrCsvParser {
    /**
     * Разбирает входящий CSV-текст и возвращает список записей.
     *
     * @param csv весь CSV (первые 5 колонок: call_type,caller_number,contact_number,start_time,end_time)
     * @return непустой список {@link CdrRecord}, или пустой список, если на входе пустая строка/только заголовки
     * @throws IllegalArgumentException при некорректном формате
     */
    List<CdrRecord> parse(String csv);
}

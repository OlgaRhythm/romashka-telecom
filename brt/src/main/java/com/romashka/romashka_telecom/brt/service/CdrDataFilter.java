package com.romashka.romashka_telecom.brt.service;

import com.romashka.romashka_telecom.brt.model.CdrRecord;

import java.util.List;

/**
 * Интерфейс для фильтрации CDR-записей.
 * Позволяет применять различные фильтры к списку записей о звонках.
 */
public interface CdrDataFilter {
    /**
     * Фильтрует список CDR-записей согласно заданным критериям.
     *
     * @param records исходный список CDR-записей
     * @return отфильтрованный список CDR-записей
     */
    List<CdrRecord> filter(List<CdrRecord> records);
}

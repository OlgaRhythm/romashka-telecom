package com.romashka.romashka_telecom.brt.service;

import com.romashka.romashka_telecom.brt.model.CdrRecord;

import java.util.List;

public interface SdrDataProcessorService {
    /**
     * Обработать список распарсенных записей.
     * @param records непустой список {@link CdrRecord}
     */
    void process(List<CdrRecord> records);
}

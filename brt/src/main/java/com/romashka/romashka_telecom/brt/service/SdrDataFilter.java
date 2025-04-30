package com.romashka.romashka_telecom.brt.service;

import com.romashka.romashka_telecom.brt.model.CdrRecord;

import java.util.List;

public interface SdrDataFilter {
    public List<CdrRecord> filter(List<CdrRecord> records);
}

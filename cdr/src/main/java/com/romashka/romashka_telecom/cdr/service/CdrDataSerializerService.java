package com.romashka.romashka_telecom.cdr.service;

import com.romashka.romashka_telecom.cdr.entity.CdrData;

import java.util.List;

public interface CdrDataSerializerService {
    String convertToCsv(List<CdrData> data);
}

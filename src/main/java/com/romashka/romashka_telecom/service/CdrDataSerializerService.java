package com.romashka.romashka_telecom.service;

import com.romashka.romashka_telecom.entity.CdrData;

import java.util.List;

public interface CdrDataSerializerService {
    String convertToCsv(List<CdrData> data);
}

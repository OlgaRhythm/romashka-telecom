package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.service.CdrDataFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Отбирает только записи, принадлежащие нашим абонентам (по номеру).
 */
@Service
@RequiredArgsConstructor
public class CdrDataFilterImpl implements CdrDataFilter {

    private final CallerRepository callerRepo;

    /**
     * Оставляем в списке только те CdrRecord, где либо callerNumber,
     * либо contactNumber встречается среди номеров наших абонентов.
     */
    @Override
    public List<CdrRecord> filter(List<CdrRecord> records) {
        // 1) Один запрос в базу: достать все наши номера
        Set<String> ourNumbers = new HashSet<>(callerRepo.findAllCallerNumbers());

        // 2) Фильтруем все записи по наличию номера в нашем множестве
        return records.stream()
                .filter(r -> ourNumbers.contains(r.getCallerNumber()))
                .collect(Collectors.toList());
    }
}

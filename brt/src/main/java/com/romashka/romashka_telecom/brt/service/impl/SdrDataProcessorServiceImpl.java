package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Call;
import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.repository.CallRepository;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.service.SdrDataFilter;
import com.romashka.romashka_telecom.brt.service.SdrDataProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SdrDataProcessorServiceImpl implements SdrDataProcessorService {

    private final SdrDataFilter filterService;
    private final CallerRepository callerRepo;
    private final CallRepository callRepo;

    @Override
    public void process(List<CdrRecord> records) {
        // Фильтрация абонентов оператора
        List<CdrRecord> filtered = filterService.filter(records);
        if (filtered.isEmpty()) {
            log.info("Нет записей CDR для обработки");
            return;
        }
        // 2) Собираем все уникальные callerNumber
        Set<String> callerNums = filtered.stream()
                .map(CdrRecord::getCallerNumber)
                .collect(Collectors.toSet());

        // 3) Один запрос в БД, чтобы получить Caller-ы по номерам
        List<Caller> callers = callerRepo.findAllByNumberIn(new ArrayList<>(callerNums));
        Map<String, Caller> byNumber = callers.stream()
                .collect(Collectors.toMap(Caller::getNumber, c -> c));

        // 4) Смапим CdrRecord → Call
        List<Call> callsToSave = new ArrayList<>(filtered.size());
        for (CdrRecord rec : filtered) {
            Caller caller = byNumber.get(rec.getCallerNumber());
            if (caller == null) {
                log.warn("Не нашли в БД абонента с номером {}, пропускаем запись", rec.getCallerNumber());
                continue;
            }
            Call call = new Call();
            call.setCallerId(caller);
            call.setContactNumber(rec.getContactNumber());
            call.setStartTime(rec.getStartTime());
            call.setEndTime(rec.getEndTime());
            callsToSave.add(call);
        }

        // 5) Сохраняем пачкой
        callRepo.saveAll(callsToSave);
        log.info("Сохранено {} звонков в таблицу calls", callsToSave.size());
    }

}

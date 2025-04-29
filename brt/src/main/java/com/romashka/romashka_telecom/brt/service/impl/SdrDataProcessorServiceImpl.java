package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.service.SdrDataProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class SdrDataProcessorServiceImpl implements SdrDataProcessorService {

    @Override
    public void process(List<CdrRecord> records) {
        records.forEach(r ->
                log.info("Обрабатываем CDR: type={}, from={} to={} [{} → {}]",
                        r.getCallType(), r.getCallerNumber(), r.getContactNumber(),
                        r.getStartTime(), r.getEndTime()
                )
        );
    }

}

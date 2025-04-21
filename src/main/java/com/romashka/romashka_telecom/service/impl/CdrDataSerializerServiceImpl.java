package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.entity.CdrData;
import com.romashka.romashka_telecom.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CdrDataSerializerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CdrDataSerializerServiceImpl implements CdrDataSerializerService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convertToCsv(List<CdrData> batch) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Writer w = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            w.write("call_type,caller_number,contact_number,start_time,end_time\n");
            for (CdrData d : batch) {
                w.write(String.format("%s,%s,%s,%s,%s%n",
                        escape(d.getCallType().name()),
                        escape(d.getCallerNumber()),
                        escape(d.getContactNumber()),
                        d.getStartTime().format(DTF),
                        d.getEndTime().format(DTF)
                ));
            }
            w.flush();
            return baos.toString(StandardCharsets.UTF_8.name());

        } catch (IOException ex) {
            throw new UncheckedIOException("CSV conversion failed", ex);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        String r = s.replace("\"", "\"\"");
        return r.contains(",") ? "\"" + r + "\"" : r;
    }
}

package com.romashka.romashka_telecom.cdr.service.impl;

import com.romashka.romashka_telecom.cdr.entity.CdrData;
import com.romashka.romashka_telecom.cdr.service.CdrDataSerializerService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
                        escape(d.getCallType().getCode()),
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

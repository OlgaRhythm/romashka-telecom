package com.romashka.romashka_telecom.brt.model;

import com.romashka.romashka_telecom.brt.enums.CallType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * DTO одной записи CDR (Call Detail Record).
 */
@Builder
@Value
public class CdrRecord {
    /**
     * Тип звонка (OUTGOING или INCOMING).
     */
    CallType callType;

    /**
     * Номер абонента, инициировавшего звонок.
     */
    String callerNumber;

    /**
     * Номер абонента, принявшего звонок.
     */
    String contactNumber;

    /**
     * Дата-время начала звонка.
     */
    LocalDateTime startTime;

    /**
     * Дата-время окончания звонка.
     */
    LocalDateTime endTime;
}

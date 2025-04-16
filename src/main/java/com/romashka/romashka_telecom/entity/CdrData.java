package com.romashka.romashka_telecom.entity;

import com.romashka.romashka_telecom.enums.CallType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cdrData")
public class CdrData {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long callId;

    /**
     * Тип звонка:
     * <ul>
     *   <li>"01" — исходящий звонок.</li>
     *   <li>"02" — входящий звонок.</li>
     * </ul>
     */
    // TODO: сделать enum
    private CallType callType;

    /**
     * Номер абонента, инициирующего звонок.
     */
    // TODO: валидация
    private String callerNumber;

    /**
     * Номер абонента, принимающего звонок.
     */
    // TODO: валидация
    private String contactNumber;

    /**
     * Дата и время начала звонка в формате ISO 8601.
     */
    private LocalDateTime startTime;

    /**
     * Дата и время окончания звонка в формате ISO 8601.
     */
    private LocalDateTime endTime;

}
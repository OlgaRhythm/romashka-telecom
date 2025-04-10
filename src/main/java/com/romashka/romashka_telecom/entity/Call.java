package com.romashka.romashka_telecom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "calls")
@NoArgsConstructor
@AllArgsConstructor
public class Call {
    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип звонка:
     * <ul>
     *   <li>"01" — исходящий звонок.</li>
     *   <li>"02" — входящий звонок.</li>
     * </ul>
     */
    private String callType;

    /**
     * Номер абонента, инициирующего звонок.
     */
    private String msisdn;

    /**
     * Номер абонента, принимающего звонок.
     */
    private String otherMsisdn;

    /**
     * Дата и время начала звонка в формате ISO 8601.
     */
    private LocalDateTime callStartTime;

    /**
     * Дата и время окончания звонка в формате ISO 8601.
     */
    private LocalDateTime callEndTime;

}
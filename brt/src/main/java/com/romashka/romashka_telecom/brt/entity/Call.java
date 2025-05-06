package com.romashka.romashka_telecom.brt.entity;

import com.romashka.romashka_telecom.brt.enums.CallType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "calls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Call {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "call_id")
    private Long callId;

    /**
     * Вызывающий абонент.  
     */
    @Column(name = "caller_id", nullable = false)
    private Long callerId;

    /**
     * Тип звонка.
     */
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    /**
     * Номер абонента, который осуществляет звонок
     */
//    @Column(name = "caller_number", nullable = false)
//    private String callerNumber;

    /**
     * Номер абонента, с которым осуществляется связь
     */
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    /**
     * Дата и время начала звонка в формате ISO 8601.
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * Дата и время окончания звонка в формате ISO 8601.
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

}

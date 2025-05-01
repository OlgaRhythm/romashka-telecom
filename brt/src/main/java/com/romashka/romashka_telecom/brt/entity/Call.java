package com.romashka.romashka_telecom.brt.entity;

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

    //TODO: добавить тип исходящий или входящий звонок

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caller_id", nullable = false)
    private Caller callerId;

    @Column(name = "contact_number")
    private String contactNumber;

    /**
     * Дата и время начала звонка в формате ISO 8601.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * Дата и время окончания звонка в формате ISO 8601.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

}

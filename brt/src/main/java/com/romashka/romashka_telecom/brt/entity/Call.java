package com.romashka.romashka_telecom.brt.entity;

import com.romashka.romashka_telecom.brt.enums.CallType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая информацию о звонке в системе.
 * Содержит данные о вызывающем абоненте, типе звонка, контактном номере
 * и времени начала и окончания звонка.
 */
@Entity
@Table(name = "calls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Call {

    /**
     * Уникальный идентификатор записи о звонке.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "call_id")
    private Long callId;

    /**
     * Идентификатор вызывающего абонента.
     */
    @Column(name = "caller_id", nullable = false)
    private Long callerId;

    /**
     * Тип звонка (исходящий/входящий).
     */
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    /**
     * Номер телефона абонента, с которым осуществляется связь.
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

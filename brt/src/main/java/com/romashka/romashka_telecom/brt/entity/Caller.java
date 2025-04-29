package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "callers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Caller {

    /**
     * Уникальный идентификатор абонента.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "caller_id")
    private Long callerId;

    /**
     * Внешний ключ на таблицу subscribers.
     */
    // TODO: нужен ли нам вся информация о абоненте?
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "subscriber_id", nullable = false)
//    private Subscriber subscriberId;
    @Column(name = "subscriber_id")
    private Long subscriberId;

    /**
     * Телефонный номер абонента.
     */
    @Column(name = "number", nullable = false, length = 20)
    private String number;

    //TODO: добавить тип исходящий или входящий звонок

    /**
     * Идентификатор тарифного плана.
     */
    @Column(name = "rate_id", nullable = false)
    private Long rateId;

    /**
     * Дата начала действия тарифа.
     */
    @Column(name = "rate_date", nullable = false)
    private LocalDateTime rateDate;
}
package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    @Column(name = "subscriber_id")
    private Long subscriberId;

    /**
     * Телефонный номер абонента.
     */
    @Column(name = "number", nullable = false, length = 20)
    private String number;

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

    /**
     * Баланс абонента.
     */
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}
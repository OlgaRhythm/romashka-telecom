package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая абонента в системе.
 * Содержит информацию о номере телефона, тарифном плане,
 * дате начала действия тарифа и текущем балансе.
 */
@Entity
@Table(name = "callers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Caller {

    /**
     * Уникальный идентификатор абонента в системе.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "caller_id")
    private Long callerId;

    /**
     * Идентификатор абонента в CRM-системе.
     * Внешний ключ на таблицу subscribers.
     */
    @Column(name = "subscriber_id")
    private Long subscriberId;

    /**
     * Телефонный номер абонента в международном формате.
     * Максимальная длина - 20 символов.
     */
    @Column(name = "number", nullable = false, length = 20)
    private String number;

    /**
     * Идентификатор тарифного плана абонента.
     */
    @Column(name = "rate_id", nullable = false)
    private Long rateId;

    /**
     * Дата и время начала действия текущего тарифного плана.
     */
    @Column(name = "rate_date", nullable = false)
    private LocalDateTime rateDate;

    /**
     * Текущий баланс абонента в рублях.
     */
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}
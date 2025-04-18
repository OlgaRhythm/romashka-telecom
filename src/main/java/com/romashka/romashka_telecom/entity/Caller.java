package com.romashka.romashka_telecom.entity;

import jakarta.persistence.*;

/**
 * Класс для представления абонента.
 * Содержит информацию о номере абонента (callerNumber).
 */
@Entity
@Table(name = "callers")
public class Caller {

    /**
     * Уникальный идентификатор абонента.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long callerId;

    /**
     * Номер телефона абонента.
     */
    // TODO: Сделать валидацию (11 символов)
    // TODO: всегда ли номер телефона уникален?
    @Column(name = "caller_number")
    private String callerNumber;

    public Caller() {}

    /**
     * Конструктор для создания абонента с указанным номером.
     *
     * @param callerNumber Номер абонента.
     */

    public Caller(String callerNumber) {
        // TODO: Сделать валидацию (11 символов)
        this.callerNumber = callerNumber;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public Object getId() {
        return callerId;
    }
}
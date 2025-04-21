package com.romashka.romashka_telecom.entity;

import com.romashka.romashka_telecom.enums.TransactionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Класс, представляющий транзакцию в системе.
 * Хранит информацию о статусе транзакции и времени её отправки.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /**
     * Уникальный идентификатор транзакции.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    /**
     * Статус транзакции.
     * Может быть одним из значений, определённых в {@link TransactionStatus}.
     */
    private TransactionStatus transactionStatus;

    /**
     * Время отправки транзакции.
     * Содержит дату и время, когда транзакция была отправлена.
     */
    private LocalDateTime sendTime;
}

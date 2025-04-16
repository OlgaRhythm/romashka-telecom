package com.romashka.romashka_telecom.entity;

import com.romashka.romashka_telecom.enums.TransactionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    /**
     * Уникальный идентификатор транзакции.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private TransactionStatus transactionStatus;

    // TODO: единый правильный формат времени
    private LocalDateTime sendTime;

}

package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.romashka.romashka_telecom.brt.enums.TransactionType;

/**
 * Сущность, представляющая денежную транзакцию в системе.
 * Используется для учета операций пополнения и списания денежных средств
 * с баланса абонента.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "money_transactions")
public class MoneyTransaction {
    
    /**
     * Уникальный идентификатор транзакции.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    
    /**
     * Идентификатор абонента, для которого выполняется транзакция.
     */
    @Column(name = "caller_id", nullable = false)
    private Long callerId;
    
    /**
     * Тип транзакции (пополнение/списание).
     */
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    /**
     * Сумма транзакции в рублях.
     */
    @Column(name = "resource_amount", nullable = false)
    private BigDecimal resourceAmount;
    
    /**
     * Дата и время выполнения транзакции.
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
} 
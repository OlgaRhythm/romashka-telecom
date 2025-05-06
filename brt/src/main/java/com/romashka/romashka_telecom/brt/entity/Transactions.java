package com.romashka.romashka_telecom.brt.entity;

import com.romashka.romashka_telecom.brt.enums.TransactionType;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transactions {

    /**
     * Уникальный идентификатор транзакции.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    /**
     * Уникальный идентификатор абонента.
     */
    @Column(name = "caller_id", nullable = false)
    private Long callerId;

    /**
     * Тип транзакции.
     * списание или пополнение
     */
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;


    /**
     * Ресурс транзакции.
     */
    @ManyToOne
    @JoinColumn(name = "resourse_type_id", nullable = false)
    private ResourseType transactionResourse;


    /**
     * Сумма транзакции.
     */ 
    @Column(name = "resource_amount", nullable = false)
    private BigDecimal resourceAmount;

    /**
     * Дата и время транзакции.
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
}

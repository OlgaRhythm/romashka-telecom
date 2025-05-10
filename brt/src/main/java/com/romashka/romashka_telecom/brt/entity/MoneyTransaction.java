package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.romashka.romashka_telecom.brt.enums.TransactionType;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "money_transactions")
public class MoneyTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    
    @Column(name = "caller_id", nullable = false)
    private Long callerId;
    
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "resource_amount", nullable = false)
    private BigDecimal resourceAmount;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

} 
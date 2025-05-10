package com.romashka.romashka_telecom.brt.repository;

import com.romashka.romashka_telecom.brt.entity.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Long> {
    
    List<MoneyTransaction> findByCallerId(Long callerId);
    
    @Query("SELECT SUM(mt.resourceAmount) FROM MoneyTransaction mt WHERE mt.callerId = :callerId")
    BigDecimal getTotalAmountByCallerId(Long callerId);
    
    @Query("SELECT mt FROM MoneyTransaction mt WHERE mt.callerId = :callerId ORDER BY mt.transactionDate DESC")
    List<MoneyTransaction> findLatestTransactionsByCallerId(Long callerId);
} 
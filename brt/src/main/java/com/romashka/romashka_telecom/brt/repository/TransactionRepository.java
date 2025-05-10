package com.romashka.romashka_telecom.brt.repository;

import com.romashka.romashka_telecom.brt.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
} 
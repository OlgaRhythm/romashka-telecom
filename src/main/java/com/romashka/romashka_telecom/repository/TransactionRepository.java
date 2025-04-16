package com.romashka.romashka_telecom.repository;

import com.romashka.romashka_telecom.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

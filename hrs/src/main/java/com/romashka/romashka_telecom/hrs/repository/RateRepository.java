package com.romashka.romashka_telecom.hrs.repository;

import com.romashka.romashka_telecom.hrs.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
} 
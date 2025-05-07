package com.romashka.romashka_telecom.hrs.repository;

import com.romashka.romashka_telecom.hrs.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallCostRepository extends JpaRepository<Rate, Long> {
}

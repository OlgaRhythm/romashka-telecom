package com.romashka.romashka_telecom.brt.repository;

import com.romashka.romashka_telecom.brt.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository extends JpaRepository<Call, Long> {
}

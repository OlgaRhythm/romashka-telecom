package com.romashka.romashka_telecom.brt.repository;

import com.romashka.romashka_telecom.brt.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
    @Query("SELECT c FROM Call c WHERE c.callerId = :callerId and c.startTime = :startTime")
    Optional<Call> findByCallerIdAndStartTime(Long callerId, LocalDateTime startTime);
}

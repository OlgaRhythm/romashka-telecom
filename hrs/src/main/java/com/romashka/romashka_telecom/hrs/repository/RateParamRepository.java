package com.romashka.romashka_telecom.hrs.repository;

import com.romashka.romashka_telecom.hrs.entity.RateParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateParamRepository extends JpaRepository<RateParam, Long> {
    @Query("SELECT rp FROM RateParam rp " +
           "JOIN Param p ON rp.paramId = p.paramId " +
           "WHERE rp.rateId = :rateId AND p.paramName = :paramName")
    Optional<RateParam> findByRateIdAndParamName(@Param("rateId") Long rateId, @Param("paramName") String paramName);
} 
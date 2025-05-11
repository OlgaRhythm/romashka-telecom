package com.romashka.romashka_telecom.hrs.repository;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.enums.CallType;
import com.romashka.romashka_telecom.hrs.enums.NetworkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallCostRepository extends JpaRepository<CallCost, Long> {
    @Query("SELECT cc FROM CallCost cc WHERE cc.rate.rateId = :rateId AND cc.callType = :callType AND cc.networkType = :networkType")
    CallCost findByRateIdAndCallTypeAndNetworkType(
            @Param("rateId") Long rateId,
            @Param("callType") CallType callType,
            @Param("networkType") NetworkType networkType
    );
}

package com.romashka.romashka_telecom.hrs.repository;

import com.romashka.romashka_telecom.hrs.entity.CallCost;
import com.romashka.romashka_telecom.hrs.enums.CallType;
import com.romashka.romashka_telecom.hrs.enums.NetworkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallCostRepository extends JpaRepository<CallCost, Long> {
    Optional<CallCost> findByCallTypeAndNetworkType(CallType callType, NetworkType networkType);
}

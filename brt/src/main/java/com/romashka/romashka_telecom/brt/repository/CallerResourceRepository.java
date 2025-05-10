package com.romashka.romashka_telecom.brt.repository;

import com.romashka.romashka_telecom.brt.entity.CallerResource;
import com.romashka.romashka_telecom.brt.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallerResourceRepository extends JpaRepository<CallerResource, Long> {
    @Query("SELECT cr FROM CallerResource cr WHERE cr.callerId = :callerId and cr.resourceId = :resource")
    Optional<CallerResource> findByCallerIdAndResource(Long callerId, Resource resource);

    List<CallerResource> findByCallerId(Long callerId);
} 
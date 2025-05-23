package com.romashka.romashka_telecom.brt.repository;

import com.romashka.romashka_telecom.brt.entity.Caller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallerRepository extends JpaRepository<Caller, Long> {

    @Query("select c.number from Caller c")
    List<String> findAllCallerNumbers();

    @Query("select c from Caller c where c.number in :numbers")
    List<Caller> findAllByNumberIn(List<String> numbers);

    Optional<Caller> findByNumber(String number);
}

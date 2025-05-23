package com.romashka.romashka_telecom.cdr.repository;

import com.romashka.romashka_telecom.cdr.entity.CdrData;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

/**
 * Репозиторий для работы с сущностью {@link CdrData}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) со звонками.
 */
@Repository
public interface CdrDataRepository extends JpaRepository<CdrData, Long> {
    Stream<CdrData> streamAllBy(Sort sort);
}

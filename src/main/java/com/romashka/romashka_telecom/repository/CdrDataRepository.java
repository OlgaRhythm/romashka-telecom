package com.romashka.romashka_telecom.repository;

import com.romashka.romashka_telecom.entity.CdrData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью {@link CdrData}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) со звонками.
 */
@Repository
public interface CdrDataRepository extends JpaRepository<CdrData, Long> {

}

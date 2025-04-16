package com.romashka.romashka_telecom.repository;

import com.romashka.romashka_telecom.entity.CdrData;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью {@link CdrData}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) со звонками.
 */
public interface CdrDataRepository extends JpaRepository<CdrData, Long> {

}

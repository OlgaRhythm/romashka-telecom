package com.romashka.romashka_telecom.repository;

import com.romashka.romashka_telecom.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью {@link Call}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) со звонками.
 */
public interface CallRepository extends JpaRepository<Call, Long> {

}

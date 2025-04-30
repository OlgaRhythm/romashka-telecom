package com.romashka.romashka_telecom.cdr.repository;


import com.romashka.romashka_telecom.cdr.entity.Caller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью {@link Caller}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) с абонентами.
 */
@Repository
public interface CallerRepository extends JpaRepository<Caller, Long> {

}
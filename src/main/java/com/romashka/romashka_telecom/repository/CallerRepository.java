package com.romashka.romashka_telecom.repository;


import com.romashka.romashka_telecom.entity.Caller;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью {@link Caller}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) с абонентами.
 */
public interface CallerRepository extends JpaRepository<Caller, Long> {

}
package com.romashka.romashka_telecom.repository;


import com.romashka.romashka_telecom.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью {@link Subscriber}.
 * Предоставляет методы для выполнения операций CRUD (Create, Read, Update, Delete) с абонентами.
 */
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

}
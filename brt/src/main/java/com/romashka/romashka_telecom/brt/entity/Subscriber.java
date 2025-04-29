package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    /**
     * Уникальный идентификатор подписчика.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriber_id")
    private Long subscriberId;

    /**
     * Читабельное имя подписчика.
     */
    @Column(name = "subscriber_name", nullable = false, length = 100)
    private String subscriberName;
}

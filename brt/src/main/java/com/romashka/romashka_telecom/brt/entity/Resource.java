package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Сущность, представляющая тип ресурса в системе.
 * Используется для определения различных типов ресурсов,
 * доступных абонентам (минуты, интернет, SMS и т.д.).
 */
@Entity
@Table(name = "resources")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    /**
     * Уникальный идентификатор типа ресурса.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    /**
     * Название ресурса (например, "минуты", "интернет", "SMS").
     * Используется для идентификации типа ресурса в системе.
     */
    @Column(name = "resource_name", nullable = false)
    private String resourceName;
}

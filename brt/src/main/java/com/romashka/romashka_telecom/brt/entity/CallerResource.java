package com.romashka.romashka_telecom.brt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Сущность, представляющая баланс ресурса абонента.
 * Связывает абонента с конкретным типом ресурса (минуты, интернет и т.д.)
 * и хранит текущий баланс этого ресурса.
 */
@Entity
@Table(name = "caller_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CallerResource {
    /**
     * Идентификатор абонента.
     * Является частью составного первичного ключа вместе с resourceId.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "caller_id")
    private Long callerId;

    /**
     * Тип ресурса.
     * Связь многие-к-одному с таблицей resources.
     */
    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resourceId;

    /**
     * Текущий баланс ресурса.
     * Может быть отрицательным в случае превышения лимита.
     */
    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;
}

package com.romashka.romashka_telecom.hrs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.romashka.romashka_telecom.hrs.enums.RateType;


@Entity
@Table(name = "rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private Long rateId;

    @Column(name = "rate_name", nullable = false)
    private String rateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false)
    private RateType rateType;

    @Column(name = "period_duration", nullable = false)
    private Long periodDuration;

    @Column(name = "period_price", nullable = false)
    private BigDecimal periodPrice;

    @Column(name = "added_minutes", nullable = false)
    private Long addedMinutes;
}

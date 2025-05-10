package com.romashka.romashka_telecom.hrs.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rate_params")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateParam {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private Long rateId;

    @Column(name = "param_id", nullable = false)
    private Long paramId;

    @Column(name = "param_value", nullable = false)
    private BigDecimal paramValue;

}

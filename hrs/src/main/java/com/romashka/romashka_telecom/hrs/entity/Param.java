package com.romashka.romashka_telecom.hrs.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "params")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Param {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "param_id")
    private Long paramId;

    @Column(name = "param_name", nullable = false)
    private String paramName;
}

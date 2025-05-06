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

@Entity
@Table(name = "resourse_type")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resourse_type_id")
    private Long resourseTypeId;

    @Column(name = "resourse_type_name", nullable = false)
    private String resourseTypeName;
}

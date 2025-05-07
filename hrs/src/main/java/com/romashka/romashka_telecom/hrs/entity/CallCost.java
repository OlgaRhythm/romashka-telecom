package com.romashka.romashka_telecom.hrs.entity;


import com.romashka.romashka_telecom.hrs.enums.CallType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "call_cost")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "call_cost_id")
    private Long callCostId;

    @Column(name = "call_type", nullable = false)
    private CallType callType;

    @Column(name = "call_cost", nullable = false)
    private BigDecimal callCost;
}

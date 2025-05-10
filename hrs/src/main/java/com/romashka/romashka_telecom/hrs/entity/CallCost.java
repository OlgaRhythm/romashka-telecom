package com.romashka.romashka_telecom.hrs.entity;


import com.romashka.romashka_telecom.hrs.enums.CallType;
import com.romashka.romashka_telecom.hrs.enums.NetworkType;
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

    @ManyToOne
    @JoinColumn(name = "rate_id", nullable = false)
    private Rate rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_type", nullable = false)
    private NetworkType networkType;

    @Column(name = "call_cost", nullable = false)
    private BigDecimal callCost;
}

package com.romashka.romashka_telecom.hrs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrsResponse {
    private Long callerId;
    private Map<String, Double> resources; // ключ - название ресурса (деньги, минуты), значение - сумма списания
} 
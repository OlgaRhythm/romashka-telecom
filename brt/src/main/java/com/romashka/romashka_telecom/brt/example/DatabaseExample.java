package com.romashka.romashka_telecom.brt.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseExample {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void executeQuery() {
        // Пример SELECT запроса
        String sql = "SELECT caller_id, number, balance FROM callers";
        
        // Выполняем запрос и получаем результат
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        // Выводим результаты
        for (Map<String, Object> row : results) {
            System.out.println("Caller ID: " + row.get("caller_id"));
            System.out.println("Number: " + row.get("number"));
            System.out.println("Balance: " + row.get("balance"));
            System.out.println("------------------------");
        }
    }
} 
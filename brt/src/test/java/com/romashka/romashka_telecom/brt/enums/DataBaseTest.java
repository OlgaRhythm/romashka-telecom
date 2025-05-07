package com.romashka.romashka_telecom.brt.enums;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataBaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void executeQuery() {
        // Пример SELECT запроса
        String sql = "SELECT caller_id, number, balance FROM callers ";

        // Выполняем запрос и получаем результат
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        // Выводим результаты
        for (Map<String, Object> row : results) {
            System.out.println("User ID: " + row.get("id"));
            System.out.println("Username: " + row.get("username"));
            System.out.println("Email: " + row.get("email"));
            System.out.println("------------------------");
        }
    }
}
package com.romashka.romashka_telecom.brt.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {

    @Autowired
    private DatabaseExample databaseExample;

    @GetMapping("/test")
    public String testDatabase() {
        databaseExample.executeQuery();
        return "Query executed successfully. Check console for results.";
    }
} 
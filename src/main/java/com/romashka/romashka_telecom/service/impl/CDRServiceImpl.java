package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.service.CDRService;
import org.springframework.stereotype.Service;


/**
 * Сервис для генерации CDR (Call Data Record) записей.
 * Генерирует тестовые данные о звонках и сохраняет их в базу данных.
 */
@Service
public class CDRServiceImpl implements CDRService {

    public void generateCDR() {
        System.out.println("Generation");
    }

}
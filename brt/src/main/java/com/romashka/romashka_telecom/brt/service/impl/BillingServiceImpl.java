package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class BillingServiceImpl implements BillingService {
    @Override
    public void chargeMonthlyFee(LocalDate modelDate) {
        log.info("⏰ [BillingService] Инициирована проверка и списание абонплаты за модельный день: {}", modelDate);
    }
}
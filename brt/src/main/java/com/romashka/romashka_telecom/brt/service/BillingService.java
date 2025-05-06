package com.romashka.romashka_telecom.brt.service;

import java.time.LocalDate;

public interface BillingService {
    void chargeMonthlyFee(LocalDate modelDate);
}

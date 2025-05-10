package com.romashka.romashka_telecom.brt.service;

import com.romashka.romashka_telecom.brt.model.BillingMessage;
import java.time.LocalDate;

public interface BillingService {
    void processAndSendBillingData(BillingMessage message);

    public void chargeMonthlyFee(LocalDate modelDate);
}

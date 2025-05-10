package com.romashka.romashka_telecom.crm.service;

import com.romashka.romashka_telecom.crm.model.PaymentRequest;
import com.romashka.romashka_telecom.crm.model.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Autowired
    private RabbitMqIntegrationService rabbitMqIntegrationService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // TODO: Реализовать логику обработки платежа
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(1L); // Временное значение
        response.setMsisdn(request.getMsisdn());
        response.setAmount(request.getAmount());
        response.setNewBalance(0.0); // Временное значение
        response.setTransactionTime(Instant.now());
        // Пример отправки события в HRS
        rabbitMqIntegrationService.sendToHrs(request);
        return response;
    }
} 
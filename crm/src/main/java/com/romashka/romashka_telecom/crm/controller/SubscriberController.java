package com.romashka.romashka_telecom.crm.controller;

import com.romashka.romashka_telecom.crm.model.PaymentRequest;
import com.romashka.romashka_telecom.crm.model.PaymentResponse;
import com.romashka.romashka_telecom.crm.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "subscriber", description = "Операции, доступные абоненту")
public class SubscriberController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    @PreAuthorize("hasRole('SUBSCRIBER')")
    @Operation(summary = "Пополнение баланса абонента")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Баланс успешно пополнен"),
        @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<PaymentResponse> addBalance(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
} 
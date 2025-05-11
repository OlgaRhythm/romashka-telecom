package com.romashka.romashka_telecom.crm.controller;

import com.romashka.romashka_telecom.crm.model.*;
import com.romashka.romashka_telecom.crm.service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "manager", description = "Операции, доступные менеджеру")
public class ManagerController {

    private final ManagerService managerService;

    @PutMapping("/changeTariff")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Изменение тарифа абонента")
    public ResponseEntity<ChangeTariffResponse> changeTariff(@RequestBody ChangeTariffRequest request) {
        return ResponseEntity.ok(managerService.changeTariff(request));
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Добавление нового абонента")
    public ResponseEntity<AddSubscriberResponse> addSubscriber(@RequestBody AddSubscriberRequest request) {
        return ResponseEntity.ok(managerService.addSubscriber(request));
    }

    @GetMapping("/getInfo")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Получить информацию об абоненте и тарифе")
    public ResponseEntity<GetInfoResponse> getSubscriberInfo(@RequestParam String msisdn) {
        return ResponseEntity.ok(managerService.getSubscriberInfo(msisdn));
    }
} 
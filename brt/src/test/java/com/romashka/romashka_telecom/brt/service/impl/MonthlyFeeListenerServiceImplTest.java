package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.entity.CallerResource;
import com.romashka.romashka_telecom.brt.entity.MoneyTransaction;
import com.romashka.romashka_telecom.brt.entity.Resource;
import com.romashka.romashka_telecom.brt.enums.TransactionType;
import com.romashka.romashka_telecom.brt.model.MonthlyFeeResponse;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.repository.CallerResourceRepository;
import com.romashka.romashka_telecom.brt.repository.MoneyTransactionRepository;
import com.romashka.romashka_telecom.brt.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MonthlyFeeListenerServiceImplTest {

    @Mock
    private CallerRepository callerRepository;
    @Mock
    private MoneyTransactionRepository moneyTransactionRepository;
    @Mock
    private CallerResourceRepository callerResourceRepository;
    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private MonthlyFeeListenerServiceImpl service;

    private Caller testCaller;
    private Resource testResource;
    private CallerResource testCallerResource;
    private MonthlyFeeResponse testResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCaller = new Caller();
        testCaller.setCallerId(1L);
        testCaller.setBalance(new BigDecimal("100.00"));
        testCaller.setRateDate(LocalDateTime.now());

        testResource = new Resource();
        testResource.setResourceId(1L);
        testResource.setResourceName("minutes");

        testCallerResource = new CallerResource();
        testCallerResource.setCallerId(1L);
        testCallerResource.setResourceId(testResource);
        testCallerResource.setCurrentBalance(new BigDecimal("50.00"));

        Map<String, Double> resources = new HashMap<>();
        resources.put("minutes", 100.0);

        testResponse = MonthlyFeeResponse.builder()
                .callerId(1L)
                .feeAmount(new BigDecimal("50.00"))
                .resources(resources)
                .build();
    }

    @Test
    void handleMonthlyFeeResponse_ShouldProcessFeeAndResources() {
        when(callerRepository.findById(1L)).thenReturn(Optional.of(testCaller));
        when(resourceRepository.findByName("minutes")).thenReturn(Optional.of(testResource));
        when(callerResourceRepository.findByCallerIdAndResource(1L, testResource)).thenReturn(Optional.of(testCallerResource));
        when(moneyTransactionRepository.save(any(MoneyTransaction.class))).thenReturn(new MoneyTransaction());
        when(callerRepository.save(any(Caller.class))).thenReturn(testCaller);
        when(callerResourceRepository.save(any(CallerResource.class))).thenReturn(testCallerResource);

        service.handleMonthlyFeeResponse(testResponse);

        verify(callerRepository).save(any(Caller.class));
        verify(moneyTransactionRepository).save(any(MoneyTransaction.class));
        verify(callerResourceRepository).save(any(CallerResource.class));
    }

    @Test
    void handleMonthlyFeeResponse_WhenNoFee_ShouldOnlyProcessResources() {
        testResponse.setFeeAmount(BigDecimal.ZERO);
        when(callerRepository.findById(1L)).thenReturn(Optional.of(testCaller));
        when(resourceRepository.findByName("minutes")).thenReturn(Optional.of(testResource));
        when(callerResourceRepository.findByCallerIdAndResource(1L, testResource)).thenReturn(Optional.of(testCallerResource));
        when(callerResourceRepository.save(any(CallerResource.class))).thenReturn(testCallerResource);

        service.handleMonthlyFeeResponse(testResponse);

        verify(callerRepository, never()).save(any(Caller.class));
        verify(moneyTransactionRepository, never()).save(any(MoneyTransaction.class));
        verify(callerResourceRepository).save(any(CallerResource.class));
    }

    @Test
    void handleMonthlyFeeResponse_WhenNoResources_ShouldOnlyProcessFee() {
        testResponse.setResources(null);
        when(callerRepository.findById(1L)).thenReturn(Optional.of(testCaller));
        when(moneyTransactionRepository.save(any(MoneyTransaction.class))).thenReturn(new MoneyTransaction());
        when(callerRepository.save(any(Caller.class))).thenReturn(testCaller);

        service.handleMonthlyFeeResponse(testResponse);

        verify(callerRepository).save(any(Caller.class));
        verify(moneyTransactionRepository).save(any(MoneyTransaction.class));
        verify(callerResourceRepository, never()).save(any(CallerResource.class));
    }

    @Test
    void handleMonthlyFeeResponse_WhenCallerNotFound_ShouldThrowException() {
        when(callerRepository.findById(1L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                service.handleMonthlyFeeResponse(testResponse)
        );
    }

    @Test
    void handleMonthlyFeeResponse_WhenResourceNotFound_ShouldThrowException() {
        when(callerRepository.findById(1L)).thenReturn(Optional.of(testCaller));
        when(resourceRepository.findByName("minutes")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                service.handleMonthlyFeeResponse(testResponse)
        );
    }
} 
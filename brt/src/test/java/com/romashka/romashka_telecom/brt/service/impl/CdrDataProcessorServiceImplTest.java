package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Call;
import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.CdrRecord;
import com.romashka.romashka_telecom.brt.repository.CallRepository;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.repository.CallerResourceRepository;
import com.romashka.romashka_telecom.brt.service.BillingService;
import com.romashka.romashka_telecom.brt.service.CdrDataFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CdrDataProcessorServiceImplTest {

    @Mock
    private BillingService billingService;
    @Mock
    private CdrDataFilter filterService;
    @Mock
    private CallerRepository callerRepo;
    @Mock
    private CallRepository callRepo;
    @Mock
    private CallerResourceRepository callerResourceRepository;

    @InjectMocks
    private CdrDataProcessorServiceImpl processor;

    private Caller testCaller;
    private CdrRecord testRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCaller = new Caller();
        testCaller.setCallerId(1L);
        testCaller.setNumber("79001234567");
        testCaller.setRateId(1L);
        testCaller.setBalance(new BigDecimal("100.00"));

        testRecord = CdrRecord.builder()
                .callType(null) // можно подставить нужный тип
                .callerNumber("79001234567")
                .contactNumber("79001234568")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Test
    void process_ShouldSaveCalls_WhenValidRecords() {
        // Arrange
        List<CdrRecord> records = Collections.singletonList(testRecord);
        when(filterService.filter(records)).thenReturn(records);
        when(callerRepo.findAllByNumberIn(eq(List.of("79001234567")))).thenReturn(List.of(testCaller));
        when(callRepo.saveAll(any())).thenReturn(Collections.singletonList(new Call()));

        // Act
        processor.process(records);

        // Assert
        verify(callRepo, times(1)).saveAll(any());
        // billingService.processAndSendBillingData НЕ вызывается в process, только в processCallsForDate
    }
} 
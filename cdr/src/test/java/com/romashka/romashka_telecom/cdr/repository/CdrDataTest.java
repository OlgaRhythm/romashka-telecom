package com.romashka.romashka_telecom.cdr.repository;

import com.romashka.romashka_telecom.cdr.service.impl.CallsGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

public class CdrDataTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CallerRepository callerRepo;

    @Mock
    private CdrDataRepository cdrRepo;

    @Mock
    private CallsGenerationServiceImpl callsGenService ;

    @Mock
    private CallsGenerationServiceImpl callsGenerationService;

    @Test
    void testGenerationCdrData () {

    }
}

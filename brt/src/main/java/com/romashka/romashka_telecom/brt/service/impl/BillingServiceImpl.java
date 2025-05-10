package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.BillingMessage;
import com.romashka.romashka_telecom.brt.model.MonthlyFeeRequest;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class BillingServiceImpl implements BillingService {
    @Qualifier("hrsRabbitTemplate")
    @Autowired
    private final RabbitTemplate hrsRabbitTemplate;

    @Autowired
    private final CallerRepository callerRepository;

    @Value("${rabbitmq.brt-to-hrs.exchange.name}")
    private String brtToHrsExchangeName;

    @Value("${rabbitmq.brt-to-hrs.routing.key}")
    private String brtToHrsRoutingKey;
    
    @Value("${rabbitmq.monthly-fee-brt-to-hrs.exchange.name}")
    private String monthlyFeeBrtToHrsExchangeName;

    @Value("${rabbitmq.monthly-fee-brt-to-hrs.routing.key}")
    private String monthlyFeeBrtToHrsRoutingKey;

//    @Value("${rabbitmq.monthly-fee-response.queue.name}")
//    private String monthlyFeeResponseQueueName;

    public BillingServiceImpl(
            @Qualifier("hrsRabbitTemplate") RabbitTemplate hrsRabbitTemplate,
            CallerRepository callerRepository
    ) {
        this.hrsRabbitTemplate = hrsRabbitTemplate;
        this.callerRepository = callerRepository;
    }

    @Override
    public void processAndSendBillingData(BillingMessage message) {
        try {
            log.info("Отправка сообщения в HRS: {}", message);
            hrsRabbitTemplate.convertAndSend(brtToHrsExchangeName, brtToHrsRoutingKey, message);
            log.info("Successfully sent billing message");
        } catch (Exception e) {
            log.error("Failed to send billing message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send billing message", e);
        }
    }

    @Override
    @Transactional
    public void chargeMonthlyFee(LocalDate modelDate) {
        log.info("⏰ [BillingService] Инициирована проверка и списание абонплаты за модельный день: {}", modelDate);
        
        List<Caller> callers = callerRepository.findAll();
        
        for (Caller caller : callers) {
            long daysSinceActivation = ChronoUnit.DAYS.between(
                caller.getRateDate().toLocalDate(),
                modelDate
            );
            
            MonthlyFeeRequest request = MonthlyFeeRequest.builder()
                .callerId(caller.getCallerId())
                .rateId(caller.getRateId())
                .daysSinceActivation((int) daysSinceActivation)
                .build();
                
            log.info("Отправка запроса на проверку абонплаты для абонента {}: {}", caller.getCallerId(), request);
            log.info("Информация об абоненте: {}", caller);
            hrsRabbitTemplate.convertAndSend(monthlyFeeBrtToHrsExchangeName, monthlyFeeBrtToHrsRoutingKey, request);
        }
    }
}
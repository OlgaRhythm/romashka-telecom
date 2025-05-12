package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.model.BillingMessage;
import com.romashka.romashka_telecom.brt.model.MonthlyFeeRequest;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.service.BillingService;
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

/**
 * Реализация сервиса для обработки биллинговых операций.
 * Отвечает за отправку данных о звонках в HRS и инициацию списания абонентской платы.
 */
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

    /**
     * Конструктор сервиса.
     *
     * @param hrsRabbitTemplate шаблон для отправки сообщений в HRS
     * @param callerRepository репозиторий для работы с абонентами
     */
    public BillingServiceImpl(
            @Qualifier("hrsRabbitTemplate") RabbitTemplate hrsRabbitTemplate,
            CallerRepository callerRepository
    ) {
        this.hrsRabbitTemplate = hrsRabbitTemplate;
        this.callerRepository = callerRepository;
    }

    /**
     * Обрабатывает и отправляет данные о звонке в HRS.
     * Отправляет сообщение в очередь для расчета стоимости звонка.
     *
     * @param message сообщение с информацией о звонке
     * @throws RuntimeException если не удалось отправить сообщение
     */
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

    /**
     * Инициирует проверку и списание абонентской платы для всех абонентов.
     * Для каждого абонента отправляет запрос в HRS с информацией о тарифе и сроке его действия.
     *
     * @param modelDate дата в модельном времени для начисления абонентской платы
     */
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
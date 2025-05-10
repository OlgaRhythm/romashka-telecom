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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyFeeListenerServiceImpl {

    private final CallerRepository callerRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final CallerResourceRepository callerResourceRepository;
    private final ResourceRepository resourceRepository;

    @RabbitListener(queues = "${rabbitmq.monthly-fee-hrs-to-brt.queue.name}")
    @Transactional
    public void handleMonthlyFeeResponse(MonthlyFeeResponse response) {
        log.info("Получен ответ на запрос абонплаты: {}", response);

        Caller caller = callerRepository.findById(response.getCallerId())
                .orElseThrow(() -> new RuntimeException("Абонент не найден: " + response.getCallerId()));

        // Обработка абонплаты
        if (response.getFeeAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Списание абонплаты
            BigDecimal newBalance = caller.getBalance().subtract(response.getFeeAmount());
            caller.setBalance(newBalance);
            callerRepository.save(caller);

            // Записываем транзакцию
            MoneyTransaction transaction = new MoneyTransaction();
            transaction.setCallerId(caller.getCallerId());
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setResourceAmount(response.getFeeAmount());
            transaction.setTransactionDate(LocalDateTime.now());
            moneyTransactionRepository.save(transaction);

            log.info("Списана абонплата для абонента {}: {}", caller.getCallerId(), response.getFeeAmount());
        } else {
            log.info("Абонплата для абонента {} не требуется", response.getCallerId());
        }

        // Обработка ресурсов
        if (response.getResources() != null) {
            for (Map.Entry<String, Double> entry : response.getResources().entrySet()) {
                String resourceName = entry.getKey();
                double amount = entry.getValue();

                Resource resource = resourceRepository.findByName(resourceName)
                        .orElseThrow(() -> new RuntimeException("Ресурс не найден: " + resourceName));

                CallerResource callerResource = callerResourceRepository
                        .findByCallerIdAndResource(caller.getCallerId(), resource)
                        .orElseThrow(() -> new RuntimeException(
                                String.format("Ресурс %s не найден для абонента %d", resourceName, caller.getCallerId())
                        ));

                // Пополняем баланс ресурса
                BigDecimal newBalance = callerResource.getCurrentBalance().add(BigDecimal.valueOf(amount));
                callerResource.setCurrentBalance(newBalance);
                callerResourceRepository.save(callerResource);

                log.info("Пополнен ресурс {} для абонента {} на {} единиц", 
                        resourceName, caller.getCallerId(), amount);
            }
        }
    }
} 
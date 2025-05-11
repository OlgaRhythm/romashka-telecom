package com.romashka.romashka_telecom.brt.service.impl;

import com.romashka.romashka_telecom.brt.entity.Caller;
import com.romashka.romashka_telecom.brt.entity.CallerResource;
import com.romashka.romashka_telecom.brt.entity.MoneyTransaction;
import com.romashka.romashka_telecom.brt.entity.Resource;
import com.romashka.romashka_telecom.brt.entity.Transaction;
import com.romashka.romashka_telecom.brt.enums.TransactionType;
import com.romashka.romashka_telecom.brt.model.HrsRequest;
import com.romashka.romashka_telecom.brt.repository.CallerRepository;
import com.romashka.romashka_telecom.brt.repository.CallerResourceRepository;
import com.romashka.romashka_telecom.brt.repository.MoneyTransactionRepository;
import com.romashka.romashka_telecom.brt.repository.ResourceRepository;
import com.romashka.romashka_telecom.brt.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrsListenerServiceImpl {

    private final CallerRepository callerRepository;
    private final CallerResourceRepository callerResourceRepository;
    private final ResourceRepository resourceTypeRepository;
    private final TransactionRepository transactionRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;

    @RabbitListener(
            queues = "${rabbitmq.hrs-to-brt.queue.name}",
            containerFactory = "hrsListenerContainerFactory"
    )
    @Transactional
    public void handleHrsMessage(HrsRequest request) {
        log.info("Получен запрос от HRS: {}", request);

        // 1. Получаем абонента
        Caller caller = callerRepository.findById(request.getCallerId())
                .orElseThrow(() -> new RuntimeException("Абонент не найден: " + request.getCallerId()));

        // 2. Обрабатываем каждый ресурс
        // проверка на null
        request.getResources().forEach((resourceName, amount) -> {
            if ("money".equals(resourceName)) { // деньги = money

                // Списание денежных средств
                BigDecimal newBalance = caller.getBalance().subtract(BigDecimal.valueOf(amount));
                caller.setBalance(newBalance);
                callerRepository.save(caller);

                // Записываем денежную транзакцию
                MoneyTransaction transaction = new MoneyTransaction();
                transaction.setCallerId(caller.getCallerId());
                transaction.setTransactionType(TransactionType.DEBIT);
                transaction.setResourceAmount(BigDecimal.valueOf(amount));
                transaction.setTransactionDate(LocalDateTime.now());
                moneyTransactionRepository.save(transaction);

            } else {
                // Списание неденежных ресурсов (минуты и др.)
                Resource resourceType = resourceTypeRepository.findByName(resourceName)
                        .orElseThrow(() -> new RuntimeException("Тип ресурса не найден: " + resourceName));
                
                CallerResource callerResource = callerResourceRepository
                        .findByCallerIdAndResource(caller.getCallerId(), resourceType)
                        .orElseThrow(() -> new RuntimeException(
                                String.format("Ресурс %s не найден для абонента %d", resourceName, caller.getCallerId())
                        ));

                // Обновляем баланс
                BigDecimal newBalance = callerResource.getCurrentBalance().subtract(BigDecimal.valueOf(amount));
                callerResource.setCurrentBalance(newBalance);
                callerResourceRepository.save(callerResource);
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    log.error("Баланс {} для ресурса {} абонента {} меньше нуля", newBalance, resourceName, caller.getCallerId());
                } else {
                    log.info("Баланс {} для ресурса {} абонента {} после списания {}", newBalance, resourceName, caller.getCallerId(), amount);
                    log.info("Баланс {} для ресурса {} абонента {} после списания {}", newBalance, resourceName, caller.getCallerId(), amount);
                }

                // Записываем транзакцию
                Transaction transaction = new Transaction();
                transaction.setCallerId(caller.getCallerId());
                transaction.setTransactionType(TransactionType.DEBIT);
                transaction.setResourceId(resourceType);
                transaction.setResourceAmount(BigDecimal.valueOf(amount));
                transaction.setTransactionDate(LocalDateTime.now());
                transactionRepository.save(transaction);
            }
        });

        log.info("Обработка запроса от HRS завершена успешно");
    }
}

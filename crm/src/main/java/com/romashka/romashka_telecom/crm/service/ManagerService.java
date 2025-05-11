package com.romashka.romashka_telecom.crm.service;

import com.romashka.romashka_telecom.crm.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ManagerService {

    @Autowired
    private RabbitMqIntegrationService rabbitMqIntegrationService;

    @Transactional
    public ChangeTariffResponse changeTariff(ChangeTariffRequest request) {
        // TODO: Реализовать логику смены тарифа
        ChangeTariffResponse response = new ChangeTariffResponse();
        response.setMsisdn(request.getMsisdn());
        response.setTariffId(request.getTariffId());
        response.setTariffDate(Instant.now());
        // Отправка события в BRT и HRS
        rabbitMqIntegrationService.sendToBrt(request);
        rabbitMqIntegrationService.sendToHrs(request);
        return response;
    }

    @Transactional
    public AddSubscriberResponse addSubscriber(AddSubscriberRequest request) {
        // TODO: Реализовать логику добавления абонента
        AddSubscriberResponse response = new AddSubscriberResponse();
        response.setSubscriberId(1L); // Временное значение
        response.setSubscriberName(request.getSubscriberName());
        response.setMsisdn(request.getMsisdn());
        response.setTariffId(request.getTariffId());
        response.setBalance(100.0); // Начальный баланс
        // Отправка события в BRT
        rabbitMqIntegrationService.sendToBrt(request);
        return response;
    }

    public GetInfoResponse getSubscriberInfo(String msisdn) {
        // TODO: Реализовать логику получения информации об абоненте
        GetInfoResponse response = new GetInfoResponse();
        
        GetInfoResponse.SubscriberInfo subscriber = new GetInfoResponse.SubscriberInfo();
        subscriber.setFullName("Тестовый Абонент");
        subscriber.setBalance(100.0);
        response.setSubscriber(subscriber);

        GetInfoResponse.TariffInfo tariff = new GetInfoResponse.TariffInfo();
        tariff.setTariffName("Тариф365");
        tariff.setTariffType("Помесячный");
        tariff.setPeriodDuration(30);
        tariff.setPeriodPrice(500);

        GetInfoResponse.TariffParameters parameters = new GetInfoResponse.TariffParameters();
        parameters.setMinutes(100);
        tariff.setParameters(parameters);
        
        response.setTariff(tariff);
        return response;
    }
} 
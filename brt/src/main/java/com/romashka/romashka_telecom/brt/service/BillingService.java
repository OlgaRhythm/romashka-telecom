package com.romashka.romashka_telecom.brt.service;

import com.romashka.romashka_telecom.brt.model.BillingMessage;
import java.time.LocalDate;

/**
 * Сервис для обработки биллинговых операций.
 * Отвечает за расчет стоимости звонков и списание абонентской платы.
 */
public interface BillingService {
    /**
     * Обрабатывает и отправляет данные о звонке для биллинга.
     * Отправляет запрос в HRS для расчета стоимости и обновляет баланс абонента.
     *
     * @param message сообщение с информацией о звонке
     */
    void processAndSendBillingData(BillingMessage message);

    /**
     * Начисляет абонентскую плату всем абонентам на указанную дату.
     * Проверяет необходимость списания абонентской платы и отправляет запрос в HRS для расчета.
     *
     * @param modelDate дата в модельном времени для начисления абонентской платы
     */
    void chargeMonthlyFee(LocalDate modelDate);
}

package com.romashka.romashka_telecom.brt.enums;

/**
 * Тип транзакции в системе биллинга.
 * Определяет направление движения средств или ресурсов.
 */
public enum TransactionType {
    /** Пополнение баланса или ресурсов */
    CREDIT,
    /** Списание средств или ресурсов */
    DEBIT
}


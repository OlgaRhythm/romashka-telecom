package com.romashka.romashka_telecom.enums;

/**
 * Перечисление, представляющее возможные статусы транзакции.
 * <ul>
 *     <li>{@link #SENT} — транзакция успешно отправлена.</li>
 *     <li>{@link #RETRIED} — транзакция была повторно отправлена.</li>
 *     <li>{@link #FAILED} — транзакция не удалась.</li>
 * </ul>
 */
public enum TransactionStatus {

    SENT,
    RETRIED,
    FAILED
}

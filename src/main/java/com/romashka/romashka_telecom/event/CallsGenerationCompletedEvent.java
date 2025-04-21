package com.romashka.romashka_telecom.event;

/**
 * Событие, которое генерируется по завершению процесса генерации звонков.
 * Содержит информацию о количестве сгенерированных записей.
 */
public class CallsGenerationCompletedEvent {

    /**
     * Количество сгенерированных записей.
     */
    private final int generatedRecords;

    /**
     * Конструктор для создания события завершения генерации звонков.
     *
     * @param generatedRecords Количество сгенерированных записей.
     */
    public CallsGenerationCompletedEvent(int generatedRecords) {
        this.generatedRecords = generatedRecords;
    }

    /**
     * Получение количества сгенерированных записей.
     *
     * @return Количество сгенерированных записей.
     */
    public int getGeneratedRecords() {
        return generatedRecords;
    }
}
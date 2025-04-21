package com.romashka.romashka_telecom.event;

public class CallsGenerationCompletedEvent {
    private final int generatedRecords;

    public CallsGenerationCompletedEvent(int generatedRecords) {
        this.generatedRecords = generatedRecords;
    }

    public int getGeneratedRecords() {
        return generatedRecords;
    }
}
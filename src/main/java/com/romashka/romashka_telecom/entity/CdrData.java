package com.romashka.romashka_telecom.entity;

import com.romashka.romashka_telecom.enums.CallType;
import jakarta.persistence.*;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;

// RabbitMQ требует, чтобы класс реализовывал Serializable

@Entity
@Table(name = "cdr_data")
public class CdrData implements Serializable {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long callId;

    /**
     * Тип звонка:
     * <ul>
     *   <li>"01" — исходящий звонок.</li>
     *   <li>"02" — входящий звонок.</li>
     * </ul>
     */
    // TODO: сделать enum
    private CallType callType;

    /**
     * Номер абонента, инициирующего звонок.
     */
    // TODO: валидация 7xxxxxxxxxx
    private String callerNumber;

    /**
     * Номер абонента, принимающего звонок.
     */
    // TODO: валидация 7xxxxxxxxxx (с какой цифры может начинаться номер?)
    private String contactNumber;

    /**
     * Дата и время начала звонка в формате ISO 8601.
     */
    // TODO: гггг-мм-ддTчч:мм:сс
    private LocalDateTime startTime;

    /**
     * Дата и время окончания звонка в формате ISO 8601.
     */
    // TODO: гггг-мм-ддTчч:мм:сс
    private LocalDateTime endTime;

    public Long getCallId() {
        return callId;
    }

//    public void setCallId(Long callId) {
//        this.callId = callId;
//    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(startTime.toString());
        out.writeObject(endTime.toString());
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        startTime = LocalDateTime.parse((String) in.readObject());
        endTime = LocalDateTime.parse((String) in.readObject());
    }

}
package com.romashka.test;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class QueueInitializer {
    private final RabbitAdmin rabbitAdmin;
    private final TopicExchange cdrExchange;
    private final DirectExchange hrsToBrtExchange;
    private final Queue cdrQueue;
    private final Queue hrsToBrtQueue;
    private final Binding cdrBinding;
    private final Binding hrsToBrtBinding;

    @Autowired
    public QueueInitializer(
            RabbitAdmin rabbitAdmin,
            TopicExchange cdrExchange,
            DirectExchange hrsToBrtExchange,
            Queue cdrQueue,
            Queue hrsToBrtQueue,
            Binding cdrBinding,
            Binding hrsToBrtBinding) {
        this.rabbitAdmin = rabbitAdmin;
        this.cdrExchange = cdrExchange;
        this.hrsToBrtExchange = hrsToBrtExchange;
        this.hrsToBrtQueue = hrsToBrtQueue;
        this.cdrQueue = cdrQueue;
        this.cdrBinding = cdrBinding;
        this.hrsToBrtBinding = hrsToBrtBinding;
    }

    @PostConstruct
    public void init() {
        // Убедимся, что очереди и обмены созданы перед запуском слушателей
        rabbitAdmin.declareExchange(cdrExchange);
        rabbitAdmin.declareExchange(hrsToBrtExchange);
        rabbitAdmin.declareQueue(cdrQueue);
        rabbitAdmin.declareQueue(hrsToBrtQueue);
        rabbitAdmin.declareBinding(cdrBinding);
        rabbitAdmin.declareBinding(hrsToBrtBinding);
    }
} 
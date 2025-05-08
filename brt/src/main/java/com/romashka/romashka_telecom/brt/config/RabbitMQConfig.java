package com.romashka.romashka_telecom.brt.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Конфигурация для интеграции с RabbitMQ: создание очереди, обмена и биндинга.
 */
@Slf4j
@Configuration
@EnableRabbit
public class RabbitMQConfig {
    private static final boolean DURABLE_QUEUE = true;

    // Конфигурация для отправки в HRS
    private static final String DEFAULT_HRS_QUEUE = "brt.queue";
    private static final String DEFAULT_HRS_EXCHANGE = "brt.exchange";
    private static final String DEFAULT_HRS_ROUTING_KEY = "brt.routingkey";

    // Конфигурация для получения от CDR
    private static final String DEFAULT_CDR_QUEUE = "cdr.queue";
    private static final String DEFAULT_CDR_EXCHANGE = "cdr.exchange";
    private static final String DEFAULT_CDR_ROUTING_KEY = "cdr.routingkey";

    @Value("${rabbitmq.hrs.queue.name:" + DEFAULT_HRS_QUEUE + "}")
    private String hrsQueueName;

    @Value("${rabbitmq.hrs.exchange.name:" + DEFAULT_HRS_EXCHANGE + "}")
    private String hrsExchangeName;

    @Value("${rabbitmq.hrs.routing.key:" + DEFAULT_HRS_ROUTING_KEY + "}")
    private String hrsRoutingKey;

    @Value("${rabbitmq.cdr.queue.name:" + DEFAULT_CDR_QUEUE + "}")
    private String cdrQueueName;

    @Value("${rabbitmq.cdr.exchange.name:" + DEFAULT_CDR_EXCHANGE + "}")
    private String cdrExchangeName;

    @Value("${rabbitmq.cdr.routing.key:" + DEFAULT_CDR_ROUTING_KEY + "}")
    private String cdrRoutingKey;

    /**
     * Выводит параметры конфигурации RabbitMQ в лог при запуске приложения.
     */
    @PostConstruct
    public void logRabbitConfig() {
        log.info("RabbitMQ Configuration:");
        log.info("HRS Queue: {}", hrsQueueName);
        log.info("HRS Exchange: {}", hrsExchangeName);
        log.info("HRS Routing key: {}", hrsRoutingKey);
        log.info("CDR Queue: {}", cdrQueueName);
        log.info("CDR Exchange: {}", cdrExchangeName);
        log.info("CDR Routing key: {}", cdrRoutingKey);
    }

    // Конфигурация для отправки в HRS
    @Bean
    public Queue hrsQueue() {
        return new Queue(hrsQueueName, DURABLE_QUEUE);
    }

    @Bean
    public TopicExchange hrsExchange() {
        return new TopicExchange(hrsExchangeName);
    }

    @Bean
    public Binding hrsBinding(Queue hrsQueue, TopicExchange hrsExchange) {
        return BindingBuilder.bind(hrsQueue)
                .to(hrsExchange)
                .with(hrsRoutingKey);
    }

    // Конфигурация для получения от CDR
    @Bean
    public Queue cdrQueue() {
        return new Queue(cdrQueueName, DURABLE_QUEUE);
    }

    @Bean
    public TopicExchange cdrExchange() {
        return new TopicExchange(cdrExchangeName);
    }

    @Bean
    public Binding cdrBinding(Queue cdrQueue, TopicExchange cdrExchange) {
        return BindingBuilder.bind(cdrQueue)
                .to(cdrExchange)
                .with(cdrRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
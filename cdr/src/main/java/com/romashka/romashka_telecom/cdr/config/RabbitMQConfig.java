package com.romashka.romashka_telecom.cdr.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для интеграции с RabbitMQ: создание очереди, обмена и биндинга.
 */
@Slf4j
@Configuration
public class RabbitMQConfig {
    private static final boolean DURABLE_QUEUE = true;

    private static final String DEFAULT_QUEUE = "cdr.queue";
    private static final String DEFAULT_EXCHANGE = "cdr.exchange";
    private static final String DEFAULT_ROUTING_KEY = "cdr.routingkey";

    @Value("${rabbitmq.queue.name:" + DEFAULT_QUEUE + "}")
    private String queueName;

    @Value("${rabbitmq.exchange.name:" + DEFAULT_EXCHANGE + "}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key:" + DEFAULT_ROUTING_KEY + "}")
    private String routingKey;

    /**
     * Выводит параметры конфигурации RabbitMQ в лог при запуске приложения.
     */
    @PostConstruct
    public void logRabbitConfig() {
        log.info("RabbitMQ Configuration:");
        log.info("Queue: {}", queueName);
        log.info("Exchange: {}", exchangeName);
        log.info("Routing key: {}", routingKey);
    }

    /**
     * Создаёт очередь для сообщений.
     *
     * @return очередь с заданным именем и флагом durability
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName, DURABLE_QUEUE);
    }

    /**
     * Создаёт topic exchange.
     *
     * @return exchange с заданным именем
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * Создаёт биндинг между очередью и exchange с указанным routing key.
     *
     * @param queue    очередь
     * @param exchange exchange
     * @return биндинг
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(routingKey);
    }

    /**
     * Создаёт простой конвертер сообщений (по умолчанию использует сериализацию объектов Java).
     *
     * @return конвертер
     */
    @Bean
    public MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }

    /**
     * Создаёт шаблон для отправки сообщений в RabbitMQ.
     *
     * @param connectionFactory фабрика подключений
     * @param converter         конвертер сообщений
     * @return RabbitTemplate с установленным конвертером
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
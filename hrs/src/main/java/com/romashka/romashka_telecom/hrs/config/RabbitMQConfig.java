package com.romashka.romashka_telecom.hrs.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    private static final String DEFAULT_QUEUE = "hrs-to-brt.queue";
    private static final String DEFAULT_EXCHANGE = "hrs-to-brt.exchange";
    private static final String DEFAULT_ROUTING_KEY = "hrs-to-brt.routingkey";
    private static final String DEFAULT_MONTHLY_FEE_QUEUE = "monthly-fee-hrs-to-brt.queue";
    private static final String DEFAULT_MONTHLY_FEE_EXCHANGE = "monthly-fee-hrs-to-brt.exchange";
    private static final String DEFAULT_MONTHLY_FEE_ROUTING_KEY = "monthly-fee-hrs-to-brt.routingkey";

    @Value("${rabbitmq.hrs-to-brt.queue.name:" + DEFAULT_QUEUE + "}")
    private String queueName;

    @Value("${rabbitmq.hrs-to-brt.exchange.name:" + DEFAULT_EXCHANGE + "}")
    private String exchangeName;

    @Value("${rabbitmq.hrs-to-brt.routing.key:" + DEFAULT_ROUTING_KEY + "}")
    private String routingKey;


    @Value("${rabbitmq.monthly-fee-hrs-to-brt.queue.name:" + DEFAULT_MONTHLY_FEE_QUEUE + "}")
    private String monthlyFeeQueueName;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.exchange.name:" + DEFAULT_MONTHLY_FEE_EXCHANGE + "}")
    private String monthlyFeeExchangeName;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.routing.key:" + DEFAULT_MONTHLY_FEE_ROUTING_KEY + "}")
    private String monthlyFeeRoutingKeyHrsToBrt;

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

    @Bean
    public Queue brtToHrsQueue(
            @Value("${rabbitmq.brt-to-hrs.queue.name}") String queueName) {
        return new Queue(queueName);
    }

    @Bean
    public DirectExchange brtToHrsExchange(
            @Value("${rabbitmq.brt-to-hrs.exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue hrsToBrtQueue(
            @Value("${rabbitmq.hrs-to-brt.queue.name}") String queueName) {
        return new Queue(queueName);
    }

    @Bean
    public DirectExchange hrsToBrtExchange(
            @Value("${rabbitmq.hrs-to-brt.exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean 
    public Queue monthlyFeeQueueBrtToHrsQueue(){
        return new Queue("monthly-fee-brt-to-hrs.queue");
    }

    @Bean
    public DirectExchange monthlyFeeBrtToHrsExchange(
            @Value("${rabbitmq.monthly-fee-brt-to-hrs.exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean 
    public Queue monthlyFeeQueueHrsToBrtQueue(){
        return new Queue("monthly-fee-hrs-to-brt.queue");
    }

    @Bean
    public DirectExchange monthlyFeeHrsToBrtExchange(
            @Value("${rabbitmq.monthly-fee-hrs-to-brt.exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }


    @Bean
    public Binding brtToHrsBinding(
            @Value("${rabbitmq.brt-to-hrs.queue.name}") String queueName,
            @Value("${rabbitmq.brt-to-hrs.exchange.name}") String exchangeName,
            @Value("${rabbitmq.brt-to-hrs.routing.key}") String routingKey) {
        return BindingBuilder.bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
    }

    @Bean
    public Binding hrsToBrtBinding(
            @Value("${rabbitmq.hrs-to-brt.queue.name}") String queueName,
            @Value("${rabbitmq.hrs-to-brt.exchange.name}") String exchangeName,
            @Value("${rabbitmq.hrs-to-brt.routing.key}") String routingKey) {
        return BindingBuilder.bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
    }

    @Bean
    public Binding monthlyFeeHrsToBrtBinding(
            @Value("${rabbitmq.monthly-fee-hrs-to-brt.queue.name}") String queueName,
            @Value("${rabbitmq.monthly-fee-hrs-to-brt.exchange.name}") String exchangeName,
            @Value("${rabbitmq.monthly-fee-hrs-to-brt.routing.key}") String routingKey) {
        return BindingBuilder.bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
    }

    @Bean
    public Binding monthlyFeeBrtToHrsBinding(
            @Value("${rabbitmq.monthly-fee-brt-to-hrs.queue.name}") String queueName,
            @Value("${rabbitmq.monthly-fee-brt-to-hrs.exchange.name}") String exchangeName,
            @Value("${rabbitmq.monthly-fee-brt-to-hrs.routing.key}") String routingKey) {
        return BindingBuilder.bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf,
            MessageConverter converter
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory cf,
            MessageConverter converter
    ) {
        var tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(converter);
        tpl.setExchange(exchangeName);  // если нужно заранее заданный exchange
        return tpl;
    }

}
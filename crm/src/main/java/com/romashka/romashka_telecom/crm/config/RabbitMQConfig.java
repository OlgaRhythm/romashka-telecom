package com.romashka.romashka_telecom.crm.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableRabbit
public class RabbitMQConfig {
    private static final boolean DURABLE_QUEUE = true;

    @Value("${rabbitmq.crm-to-brt.queue.name:crm-to-brt.queue}")
    private String crmToBrtQueueName;
    @Value("${rabbitmq.crm-to-brt.exchange.name:crm-to-brt.exchange}")
    private String crmToBrtExchangeName;
    @Value("${rabbitmq.crm-to-brt.routing.key:crm-to-brt.routingkey}")
    private String crmToBrtRoutingKey;

    @Value("${rabbitmq.brt-to-crm.queue.name:brt-to-crm.queue}")
    private String brtToCrmQueueName;
    @Value("${rabbitmq.brt-to-crm.exchange.name:brt-to-crm.exchange}")
    private String brtToCrmExchangeName;
    @Value("${rabbitmq.brt-to-crm.routing.key:brt-to-crm.routingkey}")
    private String brtToCrmRoutingKey;

    @Value("${rabbitmq.crm-to-hrs.queue.name:crm-to-hrs.queue}")
    private String crmToHrsQueueName;
    @Value("${rabbitmq.crm-to-hrs.exchange.name:crm-to-hrs.exchange}")
    private String crmToHrsExchangeName;
    @Value("${rabbitmq.crm-to-hrs.routing.key:crm-to-hrs.routingkey}")
    private String crmToHrsRoutingKey;

    @Value("${rabbitmq.hrs-to-crm.queue.name:hrs-to-crm.queue}")
    private String hrsToCrmQueueName;
    @Value("${rabbitmq.hrs-to-crm.exchange.name:hrs-to-crm.exchange}")
    private String hrsToCrmExchangeName;
    @Value("${rabbitmq.hrs-to-crm.routing.key:hrs-to-crm.routingkey}")
    private String hrsToCrmRoutingKey;

    @PostConstruct
    public void logRabbitConfig() {
        log.info("RabbitMQ CRM Configuration:");
        log.info("crm-to-brt: queue={}, exchange={}, routingKey={}", crmToBrtQueueName, crmToBrtExchangeName, crmToBrtRoutingKey);
        log.info("brt-to-crm: queue={}, exchange={}, routingKey={}", brtToCrmQueueName, brtToCrmExchangeName, brtToCrmRoutingKey);
        log.info("crm-to-hrs: queue={}, exchange={}, routingKey={}", crmToHrsQueueName, crmToHrsExchangeName, crmToHrsRoutingKey);
        log.info("hrs-to-crm: queue={}, exchange={}, routingKey={}", hrsToCrmQueueName, hrsToCrmExchangeName, hrsToCrmRoutingKey);
    }

    // Очереди
    @Bean
    public Queue crmToBrtQueue() {
        return new Queue(crmToBrtQueueName, DURABLE_QUEUE);
    }
    @Bean
    public Queue brtToCrmQueue() {
        return new Queue(brtToCrmQueueName, DURABLE_QUEUE);
    }
    @Bean
    public Queue crmToHrsQueue() {
        return new Queue(crmToHrsQueueName, DURABLE_QUEUE);
    }
    @Bean
    public Queue hrsToCrmQueue() {
        return new Queue(hrsToCrmQueueName, DURABLE_QUEUE);
    }

    // Exchanges
    @Bean
    public DirectExchange crmToBrtExchange() {
        return new DirectExchange(crmToBrtExchangeName);
    }
    @Bean
    public DirectExchange brtToCrmExchange() {
        return new DirectExchange(brtToCrmExchangeName);
    }
    @Bean
    public DirectExchange crmToHrsExchange() {
        return new DirectExchange(crmToHrsExchangeName);
    }
    @Bean
    public DirectExchange hrsToCrmExchange() {
        return new DirectExchange(hrsToCrmExchangeName);
    }

    // Bindings
    @Bean
    public Binding crmToBrtBinding() {
        return BindingBuilder.bind(crmToBrtQueue())
                .to(crmToBrtExchange())
                .with(crmToBrtRoutingKey);
    }
    @Bean
    public Binding brtToCrmBinding() {
        return BindingBuilder.bind(brtToCrmQueue())
                .to(brtToCrmExchange())
                .with(brtToCrmRoutingKey);
    }
    @Bean
    public Binding crmToHrsBinding() {
        return BindingBuilder.bind(crmToHrsQueue())
                .to(crmToHrsExchange())
                .with(crmToHrsRoutingKey);
    }
    @Bean
    public Binding hrsToCrmBinding() {
        return BindingBuilder.bind(hrsToCrmQueue())
                .to(hrsToCrmExchange())
                .with(hrsToCrmRoutingKey);
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
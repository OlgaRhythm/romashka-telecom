package com.romashka.romashka_telecom.brt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("test")
public class RabbitMQTestConfig {

    private final ConnectionFactory connectionFactory;

    @Value("${rabbitmq.cdr.exchange.name}")
    private String cdrExchangeName;

    @Value("${rabbitmq.cdr.queue.name}")
    private String cdrQueueName;

    @Value("${rabbitmq.cdr.routing.key}")
    private String cdrRoutingKey;

    @Value("${rabbitmq.brt-to-hrs.exchange.name}")
    private String brtToHrsExchangeName;

    @Value("${rabbitmq.brt-to-hrs.queue.name}")
    private String brtToHrsQueueName;

    @Value("${rabbitmq.brt-to-hrs.routing.key}")
    private String brtToHrsRoutingKey;

    @Value("${rabbitmq.hrs-to-brt.exchange.name}")
    private String hrsToBrtExchangeName;

    @Value("${rabbitmq.hrs-to-brt.queue.name}")
    private String hrsToBrtQueueName;

    @Value("${rabbitmq.hrs-to-brt.routing.key}")
    private String hrsToBrtRoutingKey;

    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        
        // Создаем обменники
        TopicExchange cdrExchange = new TopicExchange(cdrExchangeName);
        DirectExchange brtToHrsExchange = new DirectExchange(brtToHrsExchangeName);
        DirectExchange hrsToBrtExchange = new DirectExchange(hrsToBrtExchangeName);
        admin.declareExchange(cdrExchange);
        admin.declareExchange(brtToHrsExchange);
        admin.declareExchange(hrsToBrtExchange);
        log.info("Created exchanges: {}, {}, {}", cdrExchangeName, brtToHrsExchangeName, hrsToBrtExchangeName);

        // Создаем очереди
        Queue cdrQueue = new Queue(cdrQueueName, true);
        Queue brtToHrsQueue = new Queue(brtToHrsQueueName, true);
        Queue hrsToBrtQueue = new Queue(hrsToBrtQueueName, true);
        admin.declareQueue(cdrQueue);
        admin.declareQueue(brtToHrsQueue);
        admin.declareQueue(hrsToBrtQueue);
        log.info("Created queues: {}, {}, {}", cdrQueueName, brtToHrsQueueName, hrsToBrtQueueName);

        // Создаем привязки
        Binding cdrBinding = BindingBuilder.bind(cdrQueue)
                .to(cdrExchange)
                .with(cdrRoutingKey);
        Binding brtToHrsBinding = BindingBuilder.bind(brtToHrsQueue)
                .to(brtToHrsExchange)
                .with(brtToHrsRoutingKey);
        Binding hrsToBrtBinding = BindingBuilder.bind(hrsToBrtQueue)
                .to(hrsToBrtExchange)
                .with(hrsToBrtRoutingKey);
        admin.declareBinding(cdrBinding);
        admin.declareBinding(brtToHrsBinding);
        admin.declareBinding(hrsToBrtBinding);
        log.info("Created bindings for queues");

        log.info("RabbitMQ test configuration completed");
        return admin;
    }

    @Bean
    @Primary
    @Qualifier("hrsRabbitTemplate")
    public RabbitTemplate hrsRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("jsonMessageConverter") MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(brtToHrsExchangeName);
        template.setRoutingKey(brtToHrsRoutingKey);
        log.info("Configured test hrsRabbitTemplate with exchange: {} and routing key: {}", 
                brtToHrsExchangeName, brtToHrsRoutingKey);
        return template;
    }

    @Bean
    @Primary
    @Qualifier("jsonMessageConverter")
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Qualifier("simpleMessageConverter")
    public MessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }

    @Bean
    @Primary
    @Qualifier("cdrListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory cdrListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("simpleMessageConverter") MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        log.info("Configured cdrListenerContainerFactory with simpleMessageConverter");
        return factory;
    }

    @Bean
    @Primary
    @Qualifier("hrsListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory hrsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("jsonMessageConverter") MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        log.info("Configured hrsListenerContainerFactory with jsonMessageConverter");
        return factory;
    }
} 
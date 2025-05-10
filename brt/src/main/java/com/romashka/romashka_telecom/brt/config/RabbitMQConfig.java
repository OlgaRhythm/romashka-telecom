package com.romashka.romashka_telecom.brt.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
/**
 * Конфигурация для интеграции с RabbitMQ: создание очереди, обмена и биндинга.
 */
@Slf4j
@Configuration
@EnableRabbit
public class RabbitMQConfig implements RabbitListenerConfigurer {
    private static final boolean DURABLE_QUEUE = true;

    @Value("${rabbitmq.brt-to-hrs.queue.name}")
    private String brtToHrsQueueName;

    @Value("${rabbitmq.hrs-to-brt.queue.name}")
    private String hrsToBrtQueueName;

    @Value("${rabbitmq.cdr.queue.name}")
    private String cdrQueueName;

    @Value("${rabbitmq.brt-to-hrs.exchange.name}")
    private String brtToHrsExchangeName;

    @Value("${rabbitmq.hrs-to-brt.exchange.name}")
    private String hrsToBrtExchangeName;

    @Value("${rabbitmq.cdr.exchange.name}")
    private String cdrExchangeName;

    @Value("${rabbitmq.brt-to-hrs.routing.key}")
    private String brtToHrsRoutingKey;

    @Value("${rabbitmq.hrs-to-brt.routing.key}")
    private String hrsToBrtRoutingKey;

    @Value("${rabbitmq.cdr.routing.key}")
    private String cdrRoutingKey;

    /**
     * Выводит параметры конфигурации RabbitMQ в лог при запуске приложения.
     */
    @PostConstruct
    public void logRabbitConfig() {
        log.info("RabbitMQ Configuration:");
        log.info("BrtToHrs Queue: {}", brtToHrsQueueName);
        log.info("HrsToBrt Queue: {}", hrsToBrtQueueName);
        log.info("CDR Queue: {}", cdrQueueName);
        log.info("BrtToHrs Exchange: {}", brtToHrsExchangeName);
        log.info("HrsToBrt Exchange: {}", hrsToBrtExchangeName);
        log.info("CDR Exchange: {}", cdrExchangeName);
        log.info("BrtToHrs Routing key: {}", brtToHrsRoutingKey);
        log.info("HrsToBrt Routing key: {}", hrsToBrtRoutingKey);
        log.info("CDR Routing key: {}", cdrRoutingKey);
    }

    @Bean
    public Queue brtToHrsQueue() {
        return new Queue(brtToHrsQueueName, DURABLE_QUEUE);
    }

    @Bean
    public Queue hrsToBrtQueue() {
        return new Queue(hrsToBrtQueueName, DURABLE_QUEUE);
    }

    @Bean
    public Queue cdrQueue() {
        return new Queue(cdrQueueName, DURABLE_QUEUE);
    }

    @Bean
    public DirectExchange brtToHrsExchange() {
        return new DirectExchange(brtToHrsExchangeName);
    }

    @Bean
    public DirectExchange hrsToBrtExchange() {
        return new DirectExchange(hrsToBrtExchangeName);
    }

    @Bean
    public TopicExchange cdrExchange() {
        return new TopicExchange(cdrExchangeName);
    }

    @Bean
    public Binding brtToHrsBinding() {
        return BindingBuilder.bind(brtToHrsQueue())
                .to(brtToHrsExchange())
                .with(brtToHrsRoutingKey);
    }

    @Bean
    public Binding hrsToBrtBinding() {
        return BindingBuilder.bind(hrsToBrtQueue())
                .to(hrsToBrtExchange())
                .with(hrsToBrtRoutingKey);
    }

    @Bean
    public Binding cdrBinding() {
        return BindingBuilder.bind(cdrQueue())
                .to(cdrExchange())
                .with(cdrRoutingKey);
    }

    @Bean
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
    @Qualifier("cdrListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory cdrListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("simpleMessageConverter") MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    @Primary
    @Qualifier("cdrRabbitTemplate")
    public RabbitTemplate cdrRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("simpleMessageConverter") MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    @Qualifier("hrsListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory hrsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("jsonMessageConverter") MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    @Qualifier("hrsRabbitTemplate")
    public RabbitTemplate hrsRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("jsonMessageConverter") MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(hrsToBrtExchangeName);
        return template;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(new MappingJackson2MessageConverter());
        return factory;
    }
}
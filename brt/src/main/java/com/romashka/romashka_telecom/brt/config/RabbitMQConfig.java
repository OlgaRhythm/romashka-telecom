package com.romashka.romashka_telecom.brt.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
/**
 * Конфигурация для интеграции с RabbitMQ: создание очереди, обмена и биндинга.
 */
@Slf4j
@Configuration
@EnableRabbit
@RequiredArgsConstructor
@Profile("!test")  // Отключаем эту конфигурацию в тестовом окружении
public class RabbitMQConfig implements RabbitListenerConfigurer {
    private static final boolean DURABLE_QUEUE = true;

    private final ConnectionFactory connectionFactory;

    @Value("${rabbitmq.brt-to-hrs.queue.name}")
    private String brtToHrsQueueName;

    @Value("${rabbitmq.brt-to-hrs.exchange.name}")
    private String brtToHrsExchangeName;

    @Value("${rabbitmq.brt-to-hrs.routing.key}")
    private String brtToHrsRoutingKey;

    @Value("${rabbitmq.hrs-to-brt.exchange.name}")
    private String hrsToBrtExchangeName;

    @Value("${rabbitmq.hrs-to-brt.queue.name}")
    private String hrsToBrtQueueName;

    @Value("${rabbitmq.hrs-to-brt.routing.key}")
    private String hrsToBrtRoutingKey;

    @Value("${rabbitmq.monthly-fee-brt-to-hrs.queue.name}")
    private String monthlyFeeQueueBrtToHrsName;

    @Value("${rabbitmq.monthly-fee-brt-to-hrs.exchange.name}")
    private String monthlyFeeExchangeBrtToHrsName;

    @Value("${rabbitmq.monthly-fee-brt-to-hrs.routing.key}")
    private String monthlyFeeRoutingKeyBrtToHrs;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.queue.name}")
    private String monthlyFeeQueueHrsToBrtName;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.exchange.name}")
    private String monthlyFeeExchangeHrsToBrtName;

    @Value("${rabbitmq.monthly-fee-hrs-to-brt.routing.key}")
    private String monthlyFeeRoutingKeyHrsToBrt;

    @Value("${rabbitmq.cdr.queue.name}")
    private String cdrQueueName;

    @Value("${rabbitmq.cdr.exchange.name}")
    private String cdrExchangeName;

    @Value("${rabbitmq.cdr.routing.key}")
    private String cdrRoutingKey;

    /**
     * Выводит параметры конфигурации RabbitMQ в лог при запуске приложения.
     */
    @PostConstruct
    public void logRabbitConfig() {
        log.info("RabbitMQ Configuration:");

        log.info("BrtToHrs Queue: {}", brtToHrsQueueName);
        log.info("BrtToHrs Exchange: {}", brtToHrsExchangeName);
        log.info("BrtToHrs Routing key: {}", brtToHrsRoutingKey);

        log.info("HrsToBrt Queue: {}", hrsToBrtQueueName);       
        log.info("HrsToBrt Exchange: {}", hrsToBrtExchangeName);
        log.info("HrsToBrt Routing key: {}", hrsToBrtRoutingKey);

        log.info("Monthly Fee Brt to Hrs Queue: {}", monthlyFeeQueueBrtToHrsName);
        log.info("Monthly Fee Brt to Hrs Exchange: {}", monthlyFeeExchangeBrtToHrsName);
        log.info("Monthly Fee Brt to Hrs Routing key: {}", monthlyFeeRoutingKeyBrtToHrs);

        log.info("Monthly Fee Hrs to Brt Queue: {}", monthlyFeeQueueHrsToBrtName);
        log.info("Monthly Fee Hrs to Brt Exchange: {}", monthlyFeeExchangeHrsToBrtName);
        log.info("Monthly Fee Hrs to Brt Routing key: {}", monthlyFeeRoutingKeyHrsToBrt);

        log.info("CDR Queue: {}", cdrQueueName);
        log.info("CDR Exchange: {}", cdrExchangeName);
        log.info("CDR Routing key: {}", cdrRoutingKey);
    }

    // RabbitMQ Queues

    @Bean
    public Queue brtToHrsQueue() {
        return new Queue(brtToHrsQueueName, DURABLE_QUEUE);
    }

    @Bean
    public Queue hrsToBrtQueue() {
        return new Queue(hrsToBrtQueueName, DURABLE_QUEUE);
    }

    @Bean
    public Queue monthlyFeeBrtToHrsQueue() {
        return new Queue(monthlyFeeQueueBrtToHrsName, DURABLE_QUEUE);
    }

    @Bean
    public Queue monthlyFeeHrsToBrtQueue() {
        return new Queue(monthlyFeeQueueHrsToBrtName, DURABLE_QUEUE);
    }
    @Bean
    public Queue cdrQueue() {
        return new Queue(cdrQueueName, DURABLE_QUEUE);
    }

    // RabbitMQ Exchanges

    @Bean
    public DirectExchange brtToHrsExchange() {
        return new DirectExchange(brtToHrsExchangeName);
    }

    @Bean
    public DirectExchange hrsToBrtExchange() {
        return new DirectExchange(hrsToBrtExchangeName);
    }

    @Bean
    public DirectExchange monthlyFeeBrtToHrsExchange() {
        return new DirectExchange(monthlyFeeExchangeBrtToHrsName);
    }

    @Bean
    public DirectExchange monthlyFeeHrsToBrtExchange() {
        return new DirectExchange(monthlyFeeExchangeHrsToBrtName);
    }

    @Bean
    public TopicExchange cdrExchange() {
        return new TopicExchange(cdrExchangeName);
    }

    // RabbitMQ Bindings

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
    public Binding monthlyFeeBrtToHrsBinding() {
        return BindingBuilder.bind(monthlyFeeBrtToHrsQueue())
                .to(monthlyFeeBrtToHrsExchange())
                .with(monthlyFeeRoutingKeyBrtToHrs);
    }

    @Bean
    public Binding monthlyFeeHrsToBrtBinding() {
        return BindingBuilder.bind(monthlyFeeHrsToBrtQueue())
                .to(monthlyFeeHrsToBrtExchange())
                .with(monthlyFeeRoutingKeyHrsToBrt);
    }

    @Bean
    public Binding cdrBinding() {
        return BindingBuilder.bind(cdrQueue())
                .to(cdrExchange())
                .with(cdrRoutingKey);
    }

    // RabbitMQ Message Converters

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


    // RabbitMQ Listener Container Factories

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
    //@Primary
    @Qualifier("hrsRabbitTemplate")
    public RabbitTemplate hrsRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("jsonMessageConverter") MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(brtToHrsExchangeName);
        template.setRoutingKey(brtToHrsRoutingKey);
        log.info("Configured hrsRabbitTemplate with exchange: {} and routing key: {}", 
                brtToHrsExchangeName, brtToHrsRoutingKey);
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
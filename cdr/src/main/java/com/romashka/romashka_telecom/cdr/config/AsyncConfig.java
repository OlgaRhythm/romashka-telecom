package com.romashka.romashka_telecom.cdr.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация для асинхронного выполнения задач в Spring.
 * Определяет пул потоков для обработки методов, аннотированных {@code @Async}.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 50;
    private static final String THREAD_NAME_PREFIX = "AsyncEvent-";

    /**
     * Создаёт и настраивает пул потоков для асинхронных задач.
     *
     * @return настроенный {@link Executor}
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.initialize();
        return executor;
    }

    /**
     * Обработчик необработанных исключений, возникающих в асинхронных методах.
     *
     * @return {@link AsyncUncaughtExceptionHandler}
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
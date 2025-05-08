package com.romashka.romashka_telecom.cdr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);                            // n потоков
        scheduler.setThreadNamePrefix("cdr-scheduler-");      // префикс имени потока
        // дожидаемся выполнения всех задач при остановке приложения
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        // (по умолчанию у него неограниченная очередь LinkedBlockingQueue)
        scheduler.initialize();
        return scheduler;


    }
     @Override
     public void configureTasks(ScheduledTaskRegistrar registrar) {
       registrar.setScheduler(taskScheduler());
     }
}

package com.romashka.romashka_telecom.brt.config;

import com.romashka.romashka_telecom.brt.service.BillingService;
import com.romashka.romashka_telecom.common.config.TimeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Configuration
public class ScheduleBillingConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

//    @Bean
//    public void scheduleDailyBilling(TaskScheduler scheduler, TimeProperties timeProps,
//                                     BillingService billingService) {
//        // 1) Вычисляем период реальных миллисекунд, соответствующий 24 ч модельного времени
//        long periodMs = (long)( Duration.ofDays(1).toMillis() / timeProps.getCoefficient() );
//
//        // 2) Считаем "текущее модельное время"
//        long realOffsetMs = Duration.between(timeProps.getStart(), LocalDateTime.now()).toMillis();
//        long modelOffsetMs = (long)(realOffsetMs * timeProps.getCoefficient());
//        LocalDateTime nowModel = timeProps.getStart().plus(Duration.ofMillis(modelOffsetMs));
//
//        // 3) Находим следующий момент МОДЕЛЬНОЙ полуночи
//        LocalDateTime nextMidnight = nowModel.toLocalDate().plusDays(1).atStartOfDay();
//
//        // 4) Сколько реальных миллисекунд осталось до этой модельной полуночи?
//        long delayMs = (long)( Duration.between(nowModel, nextMidnight).toMillis()
//                / timeProps.getCoefficient() );
//
//        // 5) Запускаем повторение
//        scheduler.scheduleAtFixedRate(
//                () -> billingService.chargeMonthlyFee(LocalDate.now()),
//                Date.from(Instant.now().plusMillis(delayMs)),
//                periodMs
//        );
//    }
}

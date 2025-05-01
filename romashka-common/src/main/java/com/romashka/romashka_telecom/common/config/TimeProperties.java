package com.romashka.romashka_telecom.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDateTime;

@Getter
@Setter
@ConfigurationProperties(prefix = "time")
public class TimeProperties {
    /** Дата-время старта моделирования, например 2023-01-01T00:00 */
    private LocalDateTime start;
    /** Дата-время конца моделирования, например 2023-12-31T23:59 */
    private LocalDateTime end;
    /**
     * Коэффициент масштабирования времени:
     * 1.0 = реальный режим, >1.0 = ускорённая (симуляция).
     */
    private double coefficient = 1.0;
}

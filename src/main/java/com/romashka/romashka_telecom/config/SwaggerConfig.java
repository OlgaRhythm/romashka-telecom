package com.romashka.romashka_telecom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки Swagger/OpenAPI.
 * Настраивает документацию API, которая будет доступна через Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Создаёт конфигурацию OpenAPI для документации API.
     *
     * @return Объект OpenAPI с настройками документации.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Roaming CDR Service API")
                        .version("1.0")
                        .description("API для работы с CDR и UDR отчетами"));
    }
}

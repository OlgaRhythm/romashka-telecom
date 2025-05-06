package com.romashka.romashka_telecom.brt.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) throws LiquibaseException {
        try {
            // Подключение к базе данных
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(dataSource.getConnection()));

            // Снятие блокировки
            LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            lockService.forceReleaseLock();

        } catch (SQLException e) {
            // оборачиваем SQLException в LiquibaseException
            throw new LiquibaseException("Не удалось снять блокировку Liquibase", e);
        }

        // Настройка и запуск Liquibase
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");

        // удаление таблиц
        liquibase.setDropFirst(true);
        // сброс старых контрольных сумм
        liquibase.setClearCheckSums(true);

        liquibase.setShouldRun(true);
        return liquibase;
    }
}
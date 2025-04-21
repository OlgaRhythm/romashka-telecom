package com.romashka.romashka_telecom.service.impl;

import com.romashka.romashka_telecom.event.CallsGenerationCompletedEvent;
import com.romashka.romashka_telecom.service.CallsExportEventListener;
import com.romashka.romashka_telecom.service.CdrDataExportService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Обработчик событий завершения генерации CDR-записей.
 * Реагирует на {@link CallsGenerationCompletedEvent} и инициирует экспорт данных в RabbitMQ.
 */
@Slf4j
@Component
@AllArgsConstructor
public class CallsExportEventListenerImpl implements CallsExportEventListener {

    private final CdrDataExportService exportService;

    /**
     * Обработка события завершения генерации CDR-записей.
     * Вызывает экспорт данных в отдельной транзакции и потоке.
     *
     * @param event событие, содержащее информацию о количестве сгенерированных записей
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCallsGenerationComplete(CallsGenerationCompletedEvent event) {
        try {
            log.info("Starting export of {} records...", event.getGeneratedRecords());
            exportService.exportCallsData();
            log.info("Export completed successfully");
        } catch (Exception e) {
            log.error("Export failed: {}", e.getMessage(), e);
        }
    }
}

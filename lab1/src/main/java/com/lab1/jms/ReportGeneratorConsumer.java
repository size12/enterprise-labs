package com.lab1.jms;

import com.lab1.dto.ReportRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportGeneratorConsumer {

    @JmsListener(destination = "${app.queue.report}", containerFactory = "queueListenerFactory")
    public void receiveReportRequest(ReportRequestMessage message) {
        log.info("Получен запрос на генерацию отчёта: {}", message);

        try {
            // Имитация длительной операции генерации отчёта
            log.info("Начало генерации отчёта типа '{}' в формате '{}'...", 
                message.getReportType(), message.getFormat());
            Thread.sleep(3000);

            log.info("Отчёт успешно сгенерирован. Request ID: {}", message.getRequestId());

        } catch (InterruptedException e) {
            log.error("Ошибка при генерации отчёта: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to generate report", e);
        } catch (Exception e) {
            log.error("Ошибка при генерации отчёта: {}", e.getMessage());
            throw new RuntimeException("Failed to generate report", e);
        }
    }
}

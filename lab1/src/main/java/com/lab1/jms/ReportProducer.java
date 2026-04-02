package com.lab1.jms;

import com.lab1.dto.ReportRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${app.queue.report}")
    private String reportQueue;

    /**
     * Отправляет запрос на генерацию отчёта
     */
    public void requestReport(Long requestId, String reportType, String format) {
        ReportRequestMessage message = new ReportRequestMessage(requestId, reportType, format);
        log.info("Отправка запроса на генерацию отчёта в очередь {}: {}", reportQueue, message);

        jmsTemplate.convertAndSend(reportQueue, message,
            postProcessor -> {
                postProcessor.setStringProperty("messageType", "report-request");
                postProcessor.setLongProperty("requestId", requestId);
                return postProcessor;
            });
    }
}

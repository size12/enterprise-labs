package com.lab1.jms;

import com.lab1.dto.CustomerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditLogConsumer {

    @JmsListener(destination = "${app.topic.customer-events}", containerFactory = "topicListenerFactory")
    public void receiveCustomerEvent(CustomerEvent event) {
        log.info("[AUDIT LOG] Получено событие клиента: {}", event);
        
        // Имитация записи в аудит лог
        switch (event.getEventType()) {
            case CREATED:
                log.info("[AUDIT LOG] Запись: Клиент {} (ID: {}) создан. Email: {}", 
                    event.getCustomerId(), event.getCustomerId(), event.getEmail());
                break;
            case UPDATED:
                log.info("[AUDIT LOG] Запись: Клиент {} (ID: {}) обновлён. Email: {}", 
                    event.getCustomerId(), event.getCustomerId(), event.getEmail());
                break;
            case DELETED:
                log.info("[AUDIT LOG] Запись: Клиент {} (ID: {}) удалён. Email: {}", 
                    event.getCustomerId(), event.getCustomerId(), event.getEmail());
                break;
        }
        
        log.info("[AUDIT LOG] Событие успешно записано в аудит лог");
    }
}

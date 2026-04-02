package com.lab1.jms;

import com.lab1.dto.CustomerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnalyticsConsumer {

    @JmsListener(destination = "${app.topic.customer-events}", containerFactory = "topicListenerFactory")
    public void receiveCustomerEvent(CustomerEvent event) {
        log.info("[ANALYTICS] Получено событие клиента для аналитики: {}", event);
        
        // Имитация обновления аналитических данных
        switch (event.getEventType()) {
            case CREATED:
                log.info("[ANALYTICS] Инкремент счётчика новых клиентов");
                log.info("[ANALYTICS] Обновление метрики: регистрация клиента {}", event.getCustomerId());
                break;
            case UPDATED:
                log.info("[ANALYTICS] Обновление метрики: изменение данных клиента {}", event.getCustomerId());
                break;
            case DELETED:
                log.info("[ANALYTICS] Обновление метрики: удаление клиента {}", event.getCustomerId());
                break;
        }
        
        log.info("[ANALYTICS] Данные аналитики успешно обновлены");
    }
}

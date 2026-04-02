package com.lab1.jms;

import com.lab1.dto.CustomerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerEventPublisher {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${app.topic.customer-events}")
    private String customerEventsTopic;

    /**
     * Публикует событие о создании клиента
     */
    public void publishCustomerCreated(Long customerId, String email) {
        CustomerEvent event = new CustomerEvent(customerId, email, 
            com.lab1.dto.CustomerEventType.CREATED);
        log.info("Публикация события CustomerCreated в топик {}: {}", customerEventsTopic, event);
        jmsTemplate.convertAndSend(customerEventsTopic, event);
    }

    /**
     * Публикует событие об обновлении клиента
     */
    public void publishCustomerUpdated(Long customerId, String email) {
        CustomerEvent event = new CustomerEvent(customerId, email, 
            com.lab1.dto.CustomerEventType.UPDATED);
        log.info("Публикация события CustomerUpdated в топик {}: {}", customerEventsTopic, event);
        jmsTemplate.convertAndSend(customerEventsTopic, event);
    }

    /**
     * Публикует событие об удалении клиента
     */
    public void publishCustomerDeleted(Long customerId, String email) {
        CustomerEvent event = new CustomerEvent(customerId, email, 
            com.lab1.dto.CustomerEventType.DELETED);
        log.info("Публикация события CustomerDeleted в топик {}: {}", customerEventsTopic, event);
        jmsTemplate.convertAndSend(customerEventsTopic, event);
    }
}

package com.lab1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String email;
    private CustomerEventType eventType;
    private LocalDateTime occurredAt = LocalDateTime.now();

    public CustomerEvent(Long customerId, String email, CustomerEventType eventType) {
        this.customerId = customerId;
        this.email = email;
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
    }
}

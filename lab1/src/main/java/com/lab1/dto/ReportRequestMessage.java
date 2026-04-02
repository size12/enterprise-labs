package com.lab1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long requestId;
    private String reportType;
    private String format;
    private LocalDateTime requestedAt = LocalDateTime.now();

    public ReportRequestMessage(Long requestId, String reportType, String format) {
        this.requestId = requestId;
        this.reportType = reportType;
        this.format = format;
        this.requestedAt = LocalDateTime.now();
    }
}

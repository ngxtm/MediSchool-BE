package com.medischool.backend.dto;

import lombok.Data;

@Data
public class EmailTemplateResponseDTO {
    private boolean success;
    private String message;
    private int recipientCount;
    private String templateType;
    private String eventId;
} 
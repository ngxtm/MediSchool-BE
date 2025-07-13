package com.medischool.backend.dto;

import lombok.Data;

@Data
public class EmailTemplateRequestDTO {
    private String templateType;
    private String eventId;
    private String subject;
    private String content;
    private String[] recipientIds;
} 
package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.enums.CheckupConsentStatus;
import lombok.Data;

import java.util.Map;

@Data
public class CheckupConsentResponseDTO {
    private CheckupConsentStatus overallStatus;
    private String note;
    private Map<Long, String> categoryReplies; // categoryId -> "APPROVED"/"REJECTED"
}
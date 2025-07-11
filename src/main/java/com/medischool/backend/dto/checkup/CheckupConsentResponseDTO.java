package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.enums.CheckupConsentStatus;
import lombok.Data;

import java.util.Map;

@Data
public class CheckupConsentResponseDTO {
    private CheckupConsentStatus overallStatus; // APPROVED / REJECTED
    private String note; // optional
    private Map<Long, String> categoryReplies; // key: eventCategoryId, value: "APPROVED"/"REJECTED"
}
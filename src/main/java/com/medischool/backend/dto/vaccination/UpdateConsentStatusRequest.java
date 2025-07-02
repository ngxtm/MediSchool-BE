package com.medischool.backend.dto.vaccination;

import com.medischool.backend.model.enums.ConsentStatus;
import lombok.Data;

@Data
public class UpdateConsentStatusRequest {
    private ConsentStatus status;
    private String note;
} 
package com.medischool.backend.service;

import com.medischool.backend.dto.EmailTemplateRequestDTO;
import com.medischool.backend.dto.EmailTemplateResponseDTO;

public interface EmailTemplateService {
    EmailTemplateResponseDTO sendVaccinationReminder(EmailTemplateRequestDTO request);
    EmailTemplateResponseDTO sendHealthCheckupNotification(EmailTemplateRequestDTO request);
    EmailTemplateResponseDTO sendMedicationReminder(EmailTemplateRequestDTO request);
} 
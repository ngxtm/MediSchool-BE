package com.medischool.backend.service.checkup;

import com.medischool.backend.dto.checkup.CheckupConsentDTO;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import java.util.List;
import java.util.Map;

public interface CheckupConsentService {
    public List<CheckupConsentDTO> getAllConsentsForEvent(Long eventId);
    Map<String, Object> sendConsentsToAllStudents(Long eventId);
} 
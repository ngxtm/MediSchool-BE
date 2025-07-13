package com.medischool.backend.service.checkup;

import com.medischool.backend.dto.checkup.CheckupConsentDTO;
import com.medischool.backend.dto.checkup.CheckupConsentResponseDTO;
import com.medischool.backend.dto.checkup.CheckupResultDTO;
import com.medischool.backend.dto.checkup.ConsentReplyResponse;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import java.util.List;
import java.util.Map;

public interface CheckupConsentService {
    List<CheckupConsentDTO> getAllConsentsForEvent(Long eventId);
    Map<String, Object> sendConsentsToAllStudents(Long eventId);
    CheckupConsentDTO getConsentById(Long id);
    ConsentReplyResponse submitParentConsentReply(Long consentId, CheckupConsentResponseDTO dto);
    List<CheckupConsentDTO> getConsentsByStudentId(Integer studentId);
} 
package com.medischool.backend.service.checkup;

import com.medischool.backend.model.checkup.CheckupConsent;
import java.util.List;

public interface CheckupConsentService {
    List<CheckupConsent> getConsentsForStudentInEvent(Long eventId, Integer studentId);
    void submitConsents(Long eventId, Integer studentId, List<ConsentRequest> consents, Boolean fullyRejected);
    void sendConsentToAllParents(Long eventId);
    
    // Methods for parent operations
    CheckupConsent getConsentById(Long consentId);
    void submitConsentById(Long consentId, String consentStatus, String note);
    void submitAllConsentsForStudent(Long eventId, Integer studentId, String consentStatus, String note);

    class ConsentRequest {
        public Long categoryId;
        public String consentStatus;
    }
} 
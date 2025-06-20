package com.medischool.backend.service;

import com.medischool.backend.model.Vaccine.VaccinationConsent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaccineConsentService {
    private final ConsentRepository consentRepository;

    public List<VaccinationConsent> getConsentsByStudentId(Integer studentId) {
        return consentRepository.findAllByStudentId(studentId);
    }

    public VaccinationConsent updateConsentStatus(Long consentId, ConsentStatus status) {
        VaccinationConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consent not found"));

        consent.setConsentStatus(status);

        return consentRepository.save(consent);
    }

    public Map<String, Object> getConsentResultsByEvent(Long eventId) {
        List<VaccinationConsent> consents = consentRepository.findAllByEventId(eventId);

        if (consents.isEmpty()) {
            throw new RuntimeException("No consents found for event ID: " + eventId);
        }

        long totalConsents = consents.size();
        long respondedConsents = consents.stream()
                .filter(c -> c.getConsentStatus() != null)
                .count();
        long approvedConsents = consents.stream()
                .filter(c -> ConsentStatus.APPROVE.equals(c.getConsentStatus()))
                .count();
        long rejectedConsents = consents.stream()
                .filter(c -> ConsentStatus.REJECT.equals(c.getConsentStatus()))
                .count();
        long pendingConsents = totalConsents - respondedConsents;

        return Map.of(
                "eventId", eventId,
                "totalConsents", totalConsents,
                "respondedConsents", respondedConsents,
                "approvedConsents", approvedConsents,
                "rejectedConsents", rejectedConsents,
                "pendingConsents", pendingConsents,
                "responseRate", String.format("%.2f%%", (respondedConsents * 100.0) / totalConsents)
        );
    }
}
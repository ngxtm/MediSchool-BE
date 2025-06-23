package com.medischool.backend.service;

import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.medischool.backend.util.ConsentStatisticsUtil;

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

        Map<String, Object> stats = new HashMap<>(ConsentStatisticsUtil.calculate(consents));
        stats.put("eventId", eventId);
        return stats;
    }
}
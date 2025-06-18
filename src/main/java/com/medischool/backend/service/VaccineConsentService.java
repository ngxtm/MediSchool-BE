package com.medischool.backend.service;

import com.medischool.backend.model.VaccinationConsent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        consent.setReadyToSent(true);

        return consentRepository.save(consent);
    }
}
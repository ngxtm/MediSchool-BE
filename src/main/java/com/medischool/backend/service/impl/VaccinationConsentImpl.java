package com.medischool.backend.service.impl;

import com.medischool.backend.dto.vaccination.VaccineConsentDTO;
import com.medischool.backend.dto.vaccination.VaccineConsentInEvent;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.repository.*;
import com.medischool.backend.repository.vaccination.VaccinationConsentRepository;
import com.medischool.backend.repository.vaccination.VaccineEventRepository;
import com.medischool.backend.service.vaccination.VaccinationConsentService;
import com.medischool.backend.util.ConsentStatisticsUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaccinationConsentImpl implements VaccinationConsentService {
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final StudentRepository studentRepository;
    private final UserProfileRepository userProfileRepository;
    private final VaccineEventRepository vaccineEventRepository;

    @Override
    public Long getTotalConsents() {
        return vaccinationConsentRepository.count();
    }

    @Override
    public List<VaccineConsentInEvent> getVaccinationConsentsByEventId(Long eventId) {
        List<VaccinationConsent> vacConsent = vaccinationConsentRepository.getVaccinationConsentByEventId(eventId);
        List<VaccineConsentInEvent> result = new ArrayList<>();
        for (VaccinationConsent consent : vacConsent) {
            var student = studentRepository.findByStudentId(consent.getStudentId()).orElseThrow(() -> new RuntimeException("Student not found for ID: " + consent.getStudentId()));
            var parent = userProfileRepository.findById(consent.getParentId()).orElseThrow(() -> new RuntimeException("Parent not found for ID: " + consent.getParentId()));
            VaccineConsentInEvent consentDTO = VaccineConsentInEvent.converToDTO(student, parent, consent);
            result.add(consentDTO);
        }
        return result;
    }

    @Override
    public Map<String, Object> getConsentResult() {
        List<VaccinationConsent> consents = vaccinationConsentRepository.findAll();
        if (consents.isEmpty()) {
            throw new RuntimeException("No consents found");
        }

        return new HashMap<>(ConsentStatisticsUtil.calculate(consents));
    }

    public VaccineConsentDTO getVaccineConsent(Long consentId) {
        VaccinationConsent consent = vaccinationConsentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consent not found for id: " + consentId));

        var event = vaccineEventRepository.findById(consent.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found for id: " + consent.getEventId()));

        var parent = userProfileRepository.findById(consent.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found for id: " + consent.getParentId()));

        var student = studentRepository.findByStudentId(consent.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found for id: " + consent.getStudentId()));
        return VaccineConsentDTO.builder()
                .id(consent.getId())
                .event(event)
                .parent(parent)
                .student(student)
                .note(null)
                .createdAt(consent.getCreatedAt())
                .consentStatus(consent.getConsentStatus())
                .build();
    }

}

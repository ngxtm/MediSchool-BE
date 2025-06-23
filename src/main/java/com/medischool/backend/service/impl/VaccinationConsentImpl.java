package com.medischool.backend.service.impl;

import com.medischool.backend.dto.vaccine.VaccineConsentInEvent;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.VaccinationConsentRepository;
import com.medischool.backend.service.VaccinationConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.medischool.backend.util.ConsentStatisticsUtil;

@Service
@RequiredArgsConstructor
public class VaccinationConsentImpl implements VaccinationConsentService {
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final StudentRepository studentRepository;
    private final UserProfileRepository userProfileRepository;

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

}

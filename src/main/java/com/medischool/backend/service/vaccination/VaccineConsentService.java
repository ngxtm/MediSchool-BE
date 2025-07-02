package com.medischool.backend.service.vaccination;

import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.medischool.backend.util.ConsentStatisticsUtil;
import com.medischool.backend.dto.student.StudentDetailDTO;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.vaccination.VaccineEventRepository;

@Service
@RequiredArgsConstructor
public class VaccineConsentService {
    private final ConsentRepository consentRepository;
    private final VaccineEventService vaccineEventService;

    public List<VaccinationConsent> getConsentsByStudentId(Integer studentId) {
        return consentRepository.findAllByStudentId(studentId);
    }

    public VaccinationConsent updateConsentStatus(Long consentId, ConsentStatus status, String note) {
        VaccinationConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consent not found"));

        consent.setConsentStatus(status);
        if (status == ConsentStatus.REJECT) {
            consent.setNote(note);
        }

        VaccinationConsent saved = consentRepository.save(consent);

        if (status == ConsentStatus.APPROVE) {
            vaccineEventService.createVaccinationHistoryForAgreedConsents(consent.getEventId());
        }

        return saved;
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
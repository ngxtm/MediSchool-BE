package com.medischool.backend.model.vaccine;

import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.repository.VaccinationHistoryRepository;
import com.medischool.backend.repository.VaccineEventRepository;
import com.medischool.backend.util.SpringContext;

import jakarta.persistence.PostUpdate;
import java.time.LocalDateTime;
import java.util.UUID;

public class VaccinationConsentListener {

    @PostUpdate
    public void afterUpdate(VaccinationConsent consent) {
        if (consent.getConsentStatus() == ConsentStatus.APPROVE) {
            // Lấy Spring bean từ ApplicationContext
            VaccinationHistoryRepository historyRepo = SpringContext.getApplicationContext().getBean(VaccinationHistoryRepository.class);
            VaccineEventRepository eventRepo = SpringContext.getApplicationContext().getBean(VaccineEventRepository.class);

            boolean exists = historyRepo.existsByStudentIdAndEventId(consent.getStudentId(), consent.getEventId());
            if (!exists) {
                VaccineEvent event = eventRepo.findById(consent.getEventId()).orElse(null);
                if (event == null) return;

                VaccinationHistory history = new VaccinationHistory();
                history.setStudentId(consent.getStudentId());
                history.setEventId(consent.getEventId());
                history.setVaccine(event.getVaccine());
                history.setDoseNumber(1);
                history.setVaccinationDate(event.getEventDate());
                history.setLocation(event.getLocation());
                history.setNote(consent.getNote());
                history.setAbnormal(false);
                history.setFollowUpNote(null);
                history.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                history.setCreatedAt(LocalDateTime.now());

                historyRepo.save(history);
            }
        }
    }
}
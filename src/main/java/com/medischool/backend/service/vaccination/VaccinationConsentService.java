package com.medischool.backend.service.vaccination;

import com.medischool.backend.dto.vaccination.VaccineConsentDTO;
import com.medischool.backend.dto.vaccination.VaccineConsentInEvent;

import java.util.List;
import java.util.Map;

public interface VaccinationConsentService {
    Long getTotalConsents();
    List<VaccineConsentInEvent> getVaccinationConsentsByEventId(Long eventId);
    Map <String, Object> getConsentResult();
    VaccineConsentDTO getVaccineConsent(Long consentId);
    List<VaccineConsentDTO> getVaccineConsentsByStudentId(Integer studentId);
}

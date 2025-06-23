package com.medischool.backend.service;

import com.medischool.backend.dto.vaccine.VaccineConsentInEvent;

import java.util.List;
import java.util.Map;

public interface VaccinationConsentService {
    Long getTotalConsents();
    List<VaccineConsentInEvent> getVaccinationConsentsByEventId(Long eventId);
    Map <String, Object> getConsentResult();
}

package com.medischool.backend.service;

import com.medischool.backend.dto.vaccine.VaccineConsentInEvent;

import java.util.List;

public interface VaccinationConsentService {
    Long getTotalConsents();
    List<VaccineConsentInEvent> getVaccinationConsentsByEventId(Long eventId);
}

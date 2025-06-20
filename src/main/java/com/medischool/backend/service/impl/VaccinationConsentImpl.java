package com.medischool.backend.service.impl;

import com.medischool.backend.repository.VaccinationConsentRepository;
import com.medischool.backend.service.VaccinationConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VaccinationConsentImpl implements VaccinationConsentService {
    private final VaccinationConsentRepository  vaccinationConsentRepository;

    @Override
    public Long getTotalConsents() {
        return vaccinationConsentRepository.count();
    }
}

package com.medischool.backend.repository;

import com.medischool.backend.model.Vaccine.VaccinationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccinationConsentRepository extends JpaRepository<VaccinationConsent, Long> {
}

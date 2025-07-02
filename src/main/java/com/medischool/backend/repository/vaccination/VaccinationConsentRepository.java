package com.medischool.backend.repository.vaccination;

import com.medischool.backend.model.vaccine.VaccinationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationConsentRepository extends JpaRepository<VaccinationConsent, Long> {
    List<VaccinationConsent> getVaccinationConsentByEventId(Long eventId);
    List<VaccinationConsent> findAllByStudentId(Integer studentId);
}

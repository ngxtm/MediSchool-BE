package com.medischool.backend.repository;

import com.medischool.backend.model.Vaccine.VaccinationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentRepository extends JpaRepository<VaccinationConsent, Long> {
    List<VaccinationConsent> findByEventId(Long eventId);

    boolean existsByStudentIdAndEventId(Integer studentId, Long eventId);

    List<VaccinationConsent> findAllByStudentId(Integer studentId);

    List<VaccinationConsent> findAllByEventId(Long eventId);
}
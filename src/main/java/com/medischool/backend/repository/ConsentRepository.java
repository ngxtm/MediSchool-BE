package com.medischool.backend.repository;

import com.medischool.backend.model.VaccinationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentRepository extends JpaRepository<VaccinationConsent, Long> {
    List<VaccinationConsent> findByEventId(Long eventId);
    List<VaccinationConsent> findByStudentId(Integer studentId);
    boolean existsByStudentIdAndEventId(Integer studentId, Long eventId);
}
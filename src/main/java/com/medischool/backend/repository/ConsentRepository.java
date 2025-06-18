package com.medischool.backend.repository;

import com.medischool.backend.model.VaccinationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<VaccinationConsent, Long> {
    List<VaccinationConsent> findByEventId(Long eventId);

    boolean existsByStudentIdAndEventId(Integer studentId, Long eventId);

    List<VaccinationConsent> findAllByStudentId(Integer studentId);
}
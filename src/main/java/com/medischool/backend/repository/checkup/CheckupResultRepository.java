package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckupResultRepository extends JpaRepository<CheckupResult, Long> {
    boolean existsByConsentId(Long consentId);
    CheckupResult findByConsentId(Long consentId);
    List<CheckupResult> findByEventId(Long eventId);
    List<CheckupResult> findByStudent_StudentId(Integer studentId);
} 
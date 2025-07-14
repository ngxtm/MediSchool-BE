package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckupConsentRepository extends JpaRepository<CheckupConsent, Long> {
    List<CheckupConsent> findByEvent_IdAndStudent_StudentId(Long eventId, Integer studentId);
    List<CheckupConsent> findByEvent_Id(Long eventId);
    List<CheckupConsent> findByStudent_StudentId(Integer studentId);
} 
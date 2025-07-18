package com.medischool.backend.repository.checkup;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;

@Repository
public interface CheckupConsentRepository extends JpaRepository<CheckupEventConsent, Long> {
    Optional<CheckupEventConsent> findByEvent_IdAndStudent_StudentId(Long eventId, Integer studentId);
    List<CheckupEventConsent> findByEventId(Long eventId);
    List<CheckupEventConsent> findByStudent_StudentId(Integer studentId);
    long countByConsentStatus(CheckupConsentStatus checkupConsentStatus);
    int countByEvent_Id(Long eventId);
    int countByEvent_IdAndConsentStatus(Long eventId, CheckupConsentStatus status);
    List<CheckupEventConsent> findByEventIdAndIdInAndConsentStatusIsNull(Long eventId, List<Long> consentIds);
    List<CheckupEventConsent> findByEventIdAndIdInAndConsentStatus(Long eventId, List<Long> consentIds, CheckupConsentStatus status);
    List<CheckupEventConsent> findByEventIdAndConsentStatus(Long eventId, CheckupConsentStatus status);
}
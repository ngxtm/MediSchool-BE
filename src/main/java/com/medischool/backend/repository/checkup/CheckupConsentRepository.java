package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckupConsentRepository extends JpaRepository<CheckupEventConsent, Long> {
    Optional<CheckupEventConsent> findByEvent_IdAndStudent_StudentId(Long eventId, Integer studentId);
    List<CheckupEventConsent> findByEventId(Long eventId);
    List<CheckupEventConsent> findByStudent_StudentId(Integer studentId);
    long countByConsentStatus(CheckupConsentStatus checkupConsentStatus);
    int countByEvent_Id(Long eventId);
    int countByEvent_IdAndConsentStatusIn(Long eventId, Collection<CheckupConsentStatus> statuses);

}
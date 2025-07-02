package com.medischool.backend.service;

import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.repository.ConsentRepository;
import com.medischool.backend.repository.vaccination.VaccineEventRepository;
import com.medischool.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.service.vaccination.VaccineEventService;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailReminderScheduler {
    private final VaccineEventRepository vaccineEventRepository;
    private final ConsentRepository consentRepository;
    private final EmailService emailService;
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final VaccineEventService vaccineEventService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendRemindLaterEmails() {
        List<VaccineEvent> activeEvents = vaccineEventRepository.findAllByEventDateAfter(LocalDate.now().minusDays(1));
        for (VaccineEvent event : activeEvents) {
            List<VaccinationConsent> remindConsents = consentRepository.findAllByEventIdAndConsentStatus(event.getId(), ConsentStatus.REMIND_LATER);
            if (remindConsents.isEmpty()) continue;
            vaccineEventService.sendBulkEmailNotificationsForConsents(event, remindConsents);
            log.info("Sent remind-later emails for event {}: {} notifications", event.getId(), remindConsents.size());
        }
    }
} 
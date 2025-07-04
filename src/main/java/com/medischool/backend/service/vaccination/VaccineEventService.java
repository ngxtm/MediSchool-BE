package com.medischool.backend.service.vaccination;

import com.medischool.backend.dto.vaccination.VaccineEventRequestDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.*;
import com.medischool.backend.model.enums.EventStatus;
import com.medischool.backend.repository.*;
import com.medischool.backend.repository.vaccination.VaccinationHistoryRepository;
import com.medischool.backend.repository.vaccination.VaccineEventClassRepository;
import com.medischool.backend.repository.vaccination.VaccineEventRepository;
import com.medischool.backend.repository.vaccination.VaccineRepository;
import com.medischool.backend.service.AsyncEmailService;
import com.medischool.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.medischool.backend.dto.vaccination.VaccineEventEmailNotificationDTO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VaccineEventService {
    private final VaccineEventRepository vaccineEventRepository;
    private final VaccineRepository vaccineRepository;
    private final ConsentRepository consentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final VaccineEventClassRepository vaccineEventClassRepository;
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final EmailService emailService;
    private final AsyncEmailService asyncEmailService;

    private List<Integer> getAllStudentIdsInSchool() {
        return studentRepository.findAll()
                .stream()
                .map(student -> student.getStudentId())
                .collect(Collectors.toList());
    }

    private List<Integer> getAllStudentIdsInClasses(List<String> classCodes) {
        return studentRepository.findByClassCodeIn(classCodes)
                .stream()
                .map(student -> student.getStudentId())
                .collect(Collectors.toList());
    }

    private UUID getParentIdForStudent(Integer studentId) {
        return parentStudentLinkRepository.findByStudentId(studentId)
                .stream()
                .findFirst()
                .map(link -> link.getParentId())
                .orElse(null);
    }

    public VaccineEvent createVaccineEvent(VaccineEventRequestDTO requestDTO) {
        Vaccine vaccine = vaccineRepository.findById(Math.toIntExact(requestDTO.getVaccineId()))
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        VaccineEvent event = new VaccineEvent();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = auth.getName();
        UUID userId = UUID.fromString(userIdStr);

        UserProfile profileOpt = userProfileRepository.findSingleById(userId);

        event.setVaccine(vaccine);
        event.setEventTitle(requestDTO.getEventTitle());
        event.setEventDate(requestDTO.getEventDate());
        event.setEventScope(requestDTO.getEventScope());
        event.setLocation(requestDTO.getLocation());
        event.setCreatedAt(LocalDateTime.now());
        event.setCreatedBy(profileOpt);
        
        if (profileOpt != null && "MANAGER".equalsIgnoreCase(profileOpt.getRole())) {
            event.setStatus(EventStatus.APPROVED);
        } else if (requestDTO.getStatus() != null) {
            event.setStatus(requestDTO.getStatus());
        } else {
            event.setStatus(EventStatus.PENDING);
        }

        event = vaccineEventRepository.save(event);

        for (String classCode : requestDTO.getClasses()) {
            if (!vaccineEventClassRepository.existsByEventIdAndClassCode(event.getId(), classCode)) {
                VaccineEventClass link = new VaccineEventClass();
                link.setEventId(event.getId());
                link.setClassCode(classCode);
                vaccineEventClassRepository.save(link);
            }
        }
        return event;
    }

    public List<VaccineEvent> getAllVaccineEvents() {
        return vaccineEventRepository.findAll();
    }

    public VaccineEvent updateEventStatus(Long eventId, String status) {
        return updateEventStatus(eventId, status, null);
    }

    public VaccineEvent updateEventStatus(Long eventId, String status, String rejectionReason) {
        EventStatus newStatus;
        try {
            newStatus = EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be one of: APPROVED, CANCELLED, PENDING, COMPLETED");
        }

        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event not found"));

        event.setStatus(newStatus);

        if (newStatus == EventStatus.CANCELLED && rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            event.setRejectionReason(rejectionReason.trim());
        } else if (newStatus != EventStatus.CANCELLED) {
            event.setRejectionReason(null);
        }

        return vaccineEventRepository.save(event);
    }


    public Map<String, Object> sendConsentsToUnvaccinatedStudents(Long eventId) {
        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event with ID " + eventId + " not found"));

        Vaccine vaccine = event.getVaccine();
        Integer categoryId = vaccine.getCategoryId();
        Integer dosesRequired = vaccine.getDosesRequired();

        List<Integer> studentIds = new ArrayList<>();
        if ("SCHOOL".equalsIgnoreCase(event.getEventScope().toString())) {
            studentIds = getAllStudentIdsInSchool();
        } else if ("CLASS".equalsIgnoreCase(event.getEventScope().toString())) {
            List<VaccineEventClass> eventClasses = vaccineEventClassRepository.findAllByEventId(eventId);
            List<String> classCodes = eventClasses.stream().map(VaccineEventClass::getClassCode).toList();
            studentIds = getAllStudentIdsInClasses(classCodes);
        }

        int insertedCount = 0;
        for (Integer studentId : studentIds) {
            boolean consentExists = consentRepository.existsByStudentIdAndEventId(studentId, eventId);
            if (consentExists) continue;

            List<VaccinationHistory> histories = vaccinationHistoryRepository.findByStudentIdAndVaccine_CategoryId(studentId, categoryId);
            if (histories.size() >= dosesRequired) continue;

            UUID parentId = getParentIdForStudent(studentId);
            if (parentId == null) {
                continue;
            }

            VaccinationConsent consent = new VaccinationConsent();
            consent.setStudentId(studentId);
            consent.setEventId(eventId);
            consent.setParentId(parentId);
            consent.setConsentStatus(null);
            consent.setCreatedAt(LocalDateTime.now());
            consentRepository.save(consent);
            insertedCount++;
        }

        return Map.of(
                "success", true,
                "event_id", eventId,
                "message", "Consents created successfully",
                "consents_sent", insertedCount
        );
    }


    public List<VaccineEvent> getVaccineEventsByYear(int year) {
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("Invalid year");
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return vaccineEventRepository.findAllByEventDateBetween(startDate, endDate);
    }

    public Optional<VaccineEvent> getVaccineEventById(Long eventId) {
        return vaccineEventRepository.findById(eventId);
    }

    public List<VaccineEvent> getUpcomingVaccineEvent() {
        return vaccineEventRepository.findAllByEventDateAfter(LocalDate.now());
    }


    public int createVaccinationHistoryForAgreedConsents(Long eventId) {
        List<VaccinationConsent> agreedConsents = consentRepository.findAllByEventIdAndConsentStatus(eventId, ConsentStatus.APPROVE);
        Optional<VaccineEvent> eventOpt = vaccineEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) return 0;
        VaccineEvent event = eventOpt.get();
        Vaccine vaccine = event.getVaccine();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = auth.getName();
        UUID userId = UUID.fromString(userIdStr);

        UserProfile profileOpt = userProfileRepository.findSingleById(userId);

        int count = 0;
        for (VaccinationConsent consent : agreedConsents) {
            boolean exists = vaccinationHistoryRepository.existsByStudentIdAndEventId(consent.getStudentId(), eventId);
            if (!exists) {
                Integer categoryId = vaccine.getCategoryId();
                List<VaccinationHistory> histories = vaccinationHistoryRepository.findByStudentIdAndVaccine_CategoryId(consent.getStudentId(), categoryId);
                int doseNumber = histories.size() + 1;

                VaccinationHistory history = new VaccinationHistory();
                history.setStudentId(consent.getStudentId());
                history.setEventId(eventId);
                history.setVaccine(vaccine);
                history.setDoseNumber(doseNumber);

                history.setVaccinationDate(event.getEventDate());
                history.setLocation(event.getLocation());
                history.setNote(consent.getNote());
                history.setAbnormal(false);
                history.setFollowUpNote(null);

                history.setCreatedBy(profileOpt.getId());
                history.setCreatedAt(LocalDateTime.now());

                vaccinationHistoryRepository.save(history);
                count++;
            }
        }
        return count;
    }
    public VaccineEventEmailNotificationDTO sendBulkEmailNotifications2(Long eventId) {
        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event with ID " + eventId + " not found"));

        List<VaccinationConsent> pendingConsents = consentRepository.findAllByEventIdAndConsentStatusIsNull(eventId);

        if (pendingConsents.isEmpty()) {
            return VaccineEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle(event.getEventTitle())
                    .vaccineName(event.getVaccine().getName())
                    .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .eventLocation(event.getLocation())
                    .totalParentsNotified(0)
                    .totalEmailsSent(0)
                    .totalEmailsFailed(0)
                    .message("Không có parent nào cần gửi thông báo (tất cả đã có phản hồi)")
                    .success(true)
                    .build();
        }

        List<Map<String, Object>> notifications = new ArrayList<>();
        int emailsSent = 0;
        int emailsFailed = 0;

        for (VaccinationConsent consent : pendingConsents) {
            try {
                UserProfile parent = userProfileRepository.findById(consent.getParentId())
                        .orElse(null);

                if (parent == null || parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
                    emailsFailed++;
                    continue;
                }

                String studentName = "Học sinh";
                try {
                    var studentOpt = studentRepository.findByStudentId(consent.getStudentId());
                    if (studentOpt.isPresent()) {
                        studentName = studentOpt.get().getFullName();
                    }
                } catch (Exception e) {
                    studentName = "Học sinh";
                }


                String consentUrl = String.format("%s/parent/vaccination?consentId=%d",
                        System.getProperty("app.frontend.url", "http://localhost:5173"),
                        consent.getId());

                Map<String, Object> notification = Map.of(
                        "email", parent.getEmail(),
                        "parentName", parent.getFullName() != null ? parent.getFullName() : "Phụ huynh",
                        "studentName", studentName,
                        "vaccineName", event.getVaccine().getName(),
                        "eventDate", event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "eventLocation", event.getLocation(),
                        "consentUrl", consentUrl
                );

                notifications.add(notification);
                emailsSent++;

            } catch (Exception e) {
                emailsFailed++;
            }
        }


        if (!notifications.isEmpty()) {
            emailService.sendBulkVaccineConsentNotifications(notifications);
        }

        return VaccineEventEmailNotificationDTO.builder()
                .eventId(eventId)
                .eventTitle(event.getEventTitle())
                .vaccineName(event.getVaccine().getName())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .eventLocation(event.getLocation())
                .totalParentsNotified(pendingConsents.size())
                .totalEmailsSent(emailsSent)
                .totalEmailsFailed(emailsFailed)
                .notificationDetails(notifications)
                .message(String.format("Đã gửi %d email thành công, %d email thất bại", emailsSent, emailsFailed))
                .success(true)
                .build();
    }

    public VaccineEventEmailNotificationDTO sendBulkEmailNotifications(Long eventId) {
        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event with ID " + eventId + " not found"));

        List<VaccinationConsent> pendingConsents = consentRepository.findAllByEventIdAndConsentStatusIsNull(eventId);

        if (pendingConsents.isEmpty()) {
            return VaccineEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle(event.getEventTitle())
                    .vaccineName(event.getVaccine().getName())
                    .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .eventLocation(event.getLocation())
                    .totalParentsNotified(0)
                    .totalEmailsSent(0)
                    .totalEmailsFailed(0)
                    .message("Không có parent nào cần gửi thông báo (tất cả đã có phản hồi)")
                    .success(true)
                    .build();
        }

        List<Map<String, Object>> notifications = new ArrayList<>();
        int emailsSent = 0;
        int emailsFailed = 0;

        for (VaccinationConsent consent : pendingConsents) {
            try {
                UserProfile parent = userProfileRepository.findById(consent.getParentId())
                        .orElse(null);

                if (parent == null || parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
                    emailsFailed++;
                    continue;
                }

                String studentName = "Học sinh";
                try {
                    var studentOpt = studentRepository.findByStudentId(consent.getStudentId());
                    if (studentOpt.isPresent()) {
                        studentName = studentOpt.get().getFullName();
                    }
                } catch (Exception e) {
                    studentName = "Học sinh";
                }


                String consentUrl = String.format("%s/parent/vaccination?consentId=%d",
                        System.getProperty("app.frontend.url", "http://localhost:5173"),
                        consent.getId());

                Map<String, Object> notification = Map.of(
                        "email", parent.getEmail(),
                        "parentName", parent.getFullName() != null ? parent.getFullName() : "Phụ huynh",
                        "studentName", studentName,
                        "vaccineName", event.getVaccine().getName(),
                        "eventDate", event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "eventLocation", event.getLocation(),
                        "consentUrl", consentUrl
                );

                notifications.add(notification);
                emailsSent++;

            } catch (Exception e) {
                emailsFailed++;
            }
        }


        if (!notifications.isEmpty()) {
            asyncEmailService.sendBulkEmailsAsync(notifications);
        }

        return VaccineEventEmailNotificationDTO.builder()
                .eventId(eventId)
                .eventTitle(event.getEventTitle())
                .vaccineName(event.getVaccine().getName())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .eventLocation(event.getLocation())
                .totalParentsNotified(pendingConsents.size())
                .totalEmailsSent(emailsSent)
                .totalEmailsFailed(emailsFailed)
                .notificationDetails(notifications)
                .message(String.format("Đã gửi %d email thành công, %d email thất bại", emailsSent, emailsFailed))
                .success(true)
                .build();
    }

    public void sendBulkEmailNotificationsForConsents(VaccineEvent event, List<VaccinationConsent> consents) {
        if (consents == null || consents.isEmpty()) return;
        List<Map<String, Object>> notifications = new ArrayList<>();
        for (VaccinationConsent consent : consents) {
            try {
                UserProfile parent = userProfileRepository.findById(consent.getParentId())
                        .orElse(null);
                if (parent == null || parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
                    continue;
                }
                String studentName = "Học sinh";
                try {
                    var studentOpt = studentRepository.findByStudentId(consent.getStudentId());
                    if (studentOpt.isPresent()) {
                        studentName = studentOpt.get().getFullName();
                    }
                } catch (Exception e) {
                    studentName = "Học sinh";
                }
                String consentUrl = String.format("%s/parent/vaccination?consentId=%d",
                        System.getProperty("app.frontend.url", "http://localhost:5173"),
                        consent.getId());
                Map<String, Object> notification = Map.of(
                        "email", parent.getEmail(),
                        "parentName", parent.getFullName() != null ? parent.getFullName() : "Phụ huynh",
                        "studentName", studentName,
                        "vaccineName", event.getVaccine().getName(),
                        "eventDate", event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "eventLocation", event.getLocation(),
                        "consentUrl", consentUrl
                );
                notifications.add(notification);
            } catch (Exception e) {
                // Bỏ qua lỗi từng consent
            }
        }
        if (!notifications.isEmpty()) {
            asyncEmailService.sendBulkEmailsAsync(notifications);
        }
    }

}
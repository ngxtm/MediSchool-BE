package com.medischool.backend.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.healthevent.request.HealthEventEmailNotificationDTO;
import com.medischool.backend.dto.healthevent.request.HealthEventRequestDTO;
import com.medischool.backend.dto.healthevent.response.HealthEventResponseDTO;
import com.medischool.backend.dto.healthevent.response.TotalHealthEventStatusResDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.healthevent.EventMedicine;
import com.medischool.backend.model.healthevent.HealthEvent;
import com.medischool.backend.model.parentstudent.ParentStudentLink;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.healthevent.EventMedicineRepository;
import com.medischool.backend.repository.healthevent.HealthEventRepository;
import com.medischool.backend.service.AsyncEmailService;
import com.medischool.backend.service.EmailService;
import com.medischool.backend.service.healthevent.HealthEventService;
import com.medischool.backend.service.healthevent.MedicineService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthEventServiceImpl implements HealthEventService {

    private final HealthEventRepository healthEventRepository;
    private final EventMedicineRepository eventMedicineRepository;

    private final AsyncEmailService asyncEmailService;
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    private final MedicineService medicineService;
    private final EmailService emailService;

    public TotalHealthEventStatusResDTO getTotalHealthEventStatusResDTO() {
        int totalEvent = healthEventRepository.findAllWithStudent().size();
        int normalEvent = healthEventRepository.findByExtentWithStudent("NORMAL").size();
        int dangerousEvent = healthEventRepository.findByExtentWithStudent("DANGEROUS").size();

        return TotalHealthEventStatusResDTO.builder()
                .totalHealthEvent(totalEvent)
                .totalNormalCase(normalEvent)
                .totalDangerousCase(dangerousEvent)
                .build();
    }

    @Override
    public List<HealthEventResponseDTO> getAllHealthEvent() {
        List<HealthEvent> healthEvents = healthEventRepository.findAllWithStudent();
        return healthEvents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HealthEventResponseDTO convertToDTO(HealthEvent healthEvent) {
        UserProfile recordByUser = null;
        if (healthEvent.getRecordBy() != null) {
            recordByUser = userProfileRepository.findById(healthEvent.getRecordBy()).orElse(null);
        }

        return HealthEventResponseDTO.builder()
                .id(healthEvent.getId())
                .studentId(healthEvent.getStudentId())
                .student(healthEvent.getStudent())
                .problem(healthEvent.getProblem())
                .description(healthEvent.getDescription())
                .solution(healthEvent.getSolution())
                .location(healthEvent.getLocation())
                .eventTime(healthEvent.getEventTime())
                .recordBy(healthEvent.getRecordBy())
                .recordByUser(recordByUser)
                .extent(healthEvent.getExtent())
                .eventMedicines(healthEvent.getEventMedicines())
                .build();
    }

    @Override
    @Transactional
    public HealthEvent createHealthEvent(HealthEventRequestDTO requestDTO) {
        HealthEvent healthEvent = new HealthEvent();
        healthEvent.setStudentId(requestDTO.getStudentId());
        healthEvent.setProblem(requestDTO.getProblem());
        healthEvent.setDescription(requestDTO.getDescription());
        healthEvent.setSolution(requestDTO.getSolution());
        healthEvent.setLocation(requestDTO.getLocation());
        healthEvent.setEventTime(requestDTO.getEventTime() != null ? requestDTO.getEventTime() : OffsetDateTime.now());
        healthEvent.setRecordBy(requestDTO.getRecordBy());
        healthEvent.setExtent(requestDTO.getExtent());

        HealthEvent savedEvent = healthEventRepository.save(healthEvent);

        if (requestDTO.getMedicines() != null && !requestDTO.getMedicines().isEmpty()) {
            for (HealthEventRequestDTO.EventMedicineDTO medicineDTO : requestDTO.getMedicines()) {
                boolean updateSuccess = medicineService.updateMedicineQuantity(
                        medicineDTO.getMedicineId(),
                        medicineDTO.getQuantity()
                );

                if (!updateSuccess) {
                    throw new RuntimeException("Không đủ số lượng thuốc với ID: " + medicineDTO.getMedicineId() +
                                               " hoặc thuốc không tồn tại");
                }

                EventMedicine eventMedicine = new EventMedicine();
                eventMedicine.setEventId(savedEvent.getId());
                eventMedicine.setMedicineId(medicineDTO.getMedicineId());
                eventMedicine.setQuantity(medicineDTO.getQuantity());
                eventMedicine.setUnit(medicineDTO.getUnit());
                eventMedicine.setNote(medicineDTO.getNote());
                eventMedicineRepository.save(eventMedicine);
            }
        }

        return savedEvent;
    }

    @Override
    public HealthEventResponseDTO getHealthEventById(Long id) {
        HealthEvent healthEvent = healthEventRepository.findById(id).orElse(null);
        if (healthEvent == null) {
            return null;
        }
        return convertToDTO(healthEvent);
    }

    @Override
    @Transactional
    public HealthEvent updateHealthEvent(Long id, HealthEventRequestDTO requestDTO) {
        HealthEvent existingEvent = healthEventRepository.findById(id).orElse(null);
        if (existingEvent == null) {
            return null;
        }

        if (requestDTO.getStudentId() != null) {
            existingEvent.setStudentId(requestDTO.getStudentId());
        }
        if (requestDTO.getProblem() != null) {
            existingEvent.setProblem(requestDTO.getProblem());
        }
        if (requestDTO.getDescription() != null) {
            existingEvent.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getSolution() != null) {
            existingEvent.setSolution(requestDTO.getSolution());
        }
        if (requestDTO.getLocation() != null) {
            existingEvent.setLocation(requestDTO.getLocation());
        }
        if (requestDTO.getEventTime() != null) {
            existingEvent.setEventTime(requestDTO.getEventTime());
        }
        if (requestDTO.getExtent() != null) {
            existingEvent.setExtent(requestDTO.getExtent());
        }

        HealthEvent updatedEvent = healthEventRepository.save(existingEvent);

        if (requestDTO.getMedicines() != null) {
            eventMedicineRepository.deleteByEventId(id);

            for (HealthEventRequestDTO.EventMedicineDTO medicineDTO : requestDTO.getMedicines()) {
                boolean updateSuccess = medicineService.updateMedicineQuantity(
                        medicineDTO.getMedicineId(),
                        medicineDTO.getQuantity()
                );

                if (!updateSuccess) {
                    throw new RuntimeException("Không đủ số lượng thuốc với ID: " + medicineDTO.getMedicineId() +
                                               " hoặc thuốc không tồn tại");
                }

                EventMedicine eventMedicine = new EventMedicine();
                eventMedicine.setEventId(updatedEvent.getId());
                eventMedicine.setMedicineId(medicineDTO.getMedicineId());
                eventMedicine.setQuantity(medicineDTO.getQuantity());
                eventMedicine.setUnit(medicineDTO.getUnit());
                eventMedicine.setNote(medicineDTO.getNote());
                eventMedicineRepository.save(eventMedicine);
            }
        }

        return updatedEvent;
    }

    @Override
    @Transactional
    public void deleteHealthEvent(Long id) {
        eventMedicineRepository.deleteByEventId(id);
        healthEventRepository.deleteById(id);
    }

    @Override
    public HealthEventEmailNotificationDTO sendHealthEventEmailNotifications(Long eventId) {
        HealthEvent event = healthEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Health event with ID " + eventId + " not found"));

        // Lấy thông tin học sinh
        Student student = studentRepository.findById(event.getStudentId())
                .orElse(null);

        if (student == null) {
            return HealthEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle("Sự kiện y tế")
                    .studentName("Không xác định")
                    .problem(event.getProblem())
                    .description(event.getDescription())
                    .solution(event.getSolution())
                    .extent(event.getExtent())
                    .eventDate(event.getEventTime() != null ? event.getEventTime().toString() : "Không xác định")
                    .eventLocation(event.getLocation() != null ? event.getLocation() : "Trường học")
                    .totalParentsNotified(0)
                    .totalEmailsSent(0)
                    .totalEmailsFailed(0)
                    .actualCount(0)
                    .notificationDetails(new ArrayList<>())
                    .message("Không tìm thấy thông tin học sinh")
                    .success(false)
                    .build();
        }

        // Lấy danh sách phụ huynh của học sinh
        List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudentId(event.getStudentId());

        if (parentLinks.isEmpty()) {
            return HealthEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle("Sự kiện y tế")
                    .studentName(student.getFullName())
                    .problem(event.getProblem())
                    .description(event.getDescription())
                    .solution(event.getSolution())
                    .extent(event.getExtent())
                    .eventDate(event.getEventTime() != null ? event.getEventTime().toString() : "Không xác định")
                    .eventLocation(event.getLocation() != null ? event.getLocation() : "Trường học")
                    .totalParentsNotified(0)
                    .totalEmailsSent(0)
                    .totalEmailsFailed(0)
                    .actualCount(0)
                    .notificationDetails(new ArrayList<>())
                    .message("Không tìm thấy phụ huynh của học sinh")
                    .success(false)
                    .build();
        }

        List<Map<String, Object>> notifications = new ArrayList<>();
        int emailsSent = 0;
        int emailsFailed = 0;

        for (ParentStudentLink parentLink : parentLinks) {
            try {
                UserProfile parent = userProfileRepository.findById(parentLink.getParentId())
                        .orElse(null);

                if (parent == null || parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
                    emailsFailed++;
                    continue;
                }

                Map<String, Object> notification = Map.of(
                        "email", parent.getEmail(),
                        "parentName", parent.getFullName() != null ? parent.getFullName() : "Phụ huynh",
                        "studentName", student.getFullName(),
                        "problem", event.getProblem() != null ? event.getProblem() : "Không xác định",
                        "description", event.getDescription() != null ? event.getDescription() : "Không có mô tả",
                        "solution", event.getSolution() != null ? event.getSolution() : "Không có giải pháp",
                        "extent", event.getExtent() != null ? event.getExtent() : "NORMAL",
                        "eventDate", event.getEventTime() != null ? event.getEventTime().toString() : "Không xác định",
                        "eventLocation", "Trường học"
                );

                notifications.add(notification);
                emailsSent++;

            } catch (Exception e) {
                emailsFailed++;
            }
        }

        if (!notifications.isEmpty()) {
            for (Map<String, Object> notification : notifications) {
                try {
                    emailService.sendHealthEventNotification(
                            (String) notification.get("email"),
                            (String) notification.get("parentName"),
                            (String) notification.get("studentName"),
                            (String) notification.get("problem"),
                            (String) notification.get("description"),
                            (String) notification.get("solution"),
                            (String) notification.get("extent"),
                            (String) notification.get("eventDate"),
                            (String) notification.get("eventLocation")
                    );
                } catch (Exception e) {
                    log.error("Failed to send health event email to: {}", notification.get("email"), e);
                }
            }
        }

        return HealthEventEmailNotificationDTO.builder()
                .eventId(eventId)
                .eventTitle("Sự kiện y tế")
                .studentName(student.getFullName())
                .problem(event.getProblem())
                .description(event.getDescription())
                .solution(event.getSolution())
                .extent(event.getExtent())
                .eventDate(event.getEventTime() != null ? event.getEventTime().toString() : "Không xác định")
                .eventLocation(event.getLocation() != null ? event.getLocation() : "Trường học")
                .totalParentsNotified(parentLinks.size())
                .totalEmailsSent(emailsSent)
                .totalEmailsFailed(emailsFailed)
                .actualCount(emailsSent)
                .notificationDetails(notifications)
                .message(String.format("Đã gửi %d email thành công, %d email thất bại", emailsSent, emailsFailed))
                .success(true)
                .build();
    }

    @Override
    public List<HealthEventEmailNotificationDTO> sendAllHealthEventEmailNotifications() {
        List<HealthEvent> allEvents = healthEventRepository.findAll();
        List<HealthEventEmailNotificationDTO> results = new ArrayList<>();

        for (HealthEvent event : allEvents) {
            try {
                HealthEventEmailNotificationDTO result = sendHealthEventEmailNotifications(event.getId());
                results.add(result);
            } catch (Exception e) {
                HealthEventEmailNotificationDTO errorResult = HealthEventEmailNotificationDTO.builder()
                        .eventId(event.getId())
                        .eventTitle("Sự kiện y tế")
                        .studentName("Không xác định")
                        .problem(event.getProblem())
                        .description(event.getDescription())
                        .solution(event.getSolution())
                        .extent(event.getExtent())
                        .eventDate(event.getEventTime() != null ? event.getEventTime().toString() : "Không xác định")
                        .eventLocation(event.getLocation() != null ? event.getLocation() : "Trường học")
                        .totalParentsNotified(0)
                        .totalEmailsSent(0)
                        .totalEmailsFailed(1)
                        .actualCount(0)
                        .notificationDetails(new ArrayList<>())
                        .message("Lỗi khi gửi email: " + e.getMessage())
                        .success(false)
                        .build();
                results.add(errorResult);
            }
        }

        return results;
    }

    @Override
    @Transactional
    public HealthEventEmailNotificationDTO sendSelectiveHealthEventEmailNotifications(Long eventId, List<Integer> studentIds) {
        HealthEvent healthEvent = null;
        try {
            // Validate event exists
            healthEvent = healthEventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Health event not found with ID: " + eventId));

            // Get students for the specified IDs
            List<Student> studentsToEmail = studentRepository.findAllById(studentIds);

            if (studentsToEmail.isEmpty()) {
                return HealthEventEmailNotificationDTO.builder()
                        .eventId(eventId)
                        .eventTitle("Sự kiện y tế")
                        .studentName("Không xác định")
                        .problem(healthEvent.getProblem())
                        .description(healthEvent.getDescription())
                        .solution(healthEvent.getSolution())
                        .extent(healthEvent.getExtent())
                        .eventDate(healthEvent.getEventTime() != null ? healthEvent.getEventTime().toString() : "Không xác định")
                        .eventLocation(healthEvent.getLocation() != null ? healthEvent.getLocation() : "Trường học")
                        .totalParentsNotified(0)
                        .totalEmailsSent(0)
                        .totalEmailsFailed(0)
                        .actualCount(0)
                        .notificationDetails(new ArrayList<>())
                        .message("No students found for the selected records")
                        .success(true)
                        .build();
            }

            // Prepare email notifications
            List<Map<String, Object>> emailNotifications = new ArrayList<>();
            int totalParentsFound = 0;

            for (Student student : studentsToEmail) {
                // Get parent information for this student
                List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudentId(student.getStudentId());

                for (ParentStudentLink parentLink : parentLinks) {
                    UserProfile parent = userProfileRepository.findById(parentLink.getParentId()).orElse(null);

                    if (parent != null && parent.getEmail() != null && !parent.getEmail().trim().isEmpty()) {
                        Map<String, Object> emailData = new HashMap<>();
                        emailData.put("toEmail", parent.getEmail());
                        emailData.put("parentName", parent.getFullName() != null ? parent.getFullName() : "Phụ huynh");
                        emailData.put("studentName", student.getFullName());
                        emailData.put("problem", healthEvent.getProblem() != null ? healthEvent.getProblem() : "Không xác định");
                        emailData.put("description", healthEvent.getDescription() != null ? healthEvent.getDescription() : "Không có mô tả");
                        emailData.put("solution", healthEvent.getSolution() != null ? healthEvent.getSolution() : "Không có giải pháp");
                        emailData.put("extent", healthEvent.getExtent() != null ? healthEvent.getExtent() : "NORMAL");
                        emailData.put("eventDate", healthEvent.getEventTime() != null ? healthEvent.getEventTime().toString() : "Không xác định");
                        emailData.put("eventLocation", healthEvent.getLocation() != null ? healthEvent.getLocation() : "Trường học");
                        emailData.put("studentId", student.getStudentId());

                        emailNotifications.add(emailData);
                        totalParentsFound++;
                    }
                }
            }

            if (emailNotifications.isEmpty()) {
                return HealthEventEmailNotificationDTO.builder()
                        .eventId(eventId)
                        .eventTitle("Sự kiện y tế")
                        .studentName(healthEvent.getStudent() != null ? healthEvent.getStudent().getFullName() : "Không xác định")
                        .problem(healthEvent.getProblem())
                        .description(healthEvent.getDescription())
                        .solution(healthEvent.getSolution())
                        .extent(healthEvent.getExtent())
                        .eventDate(healthEvent.getEventTime() != null ? healthEvent.getEventTime().toString() : "Không xác định")
                        .eventLocation(healthEvent.getLocation() != null ? healthEvent.getLocation() : "Trường học")
                        .totalParentsNotified(0)
                        .totalEmailsSent(0)
                        .totalEmailsFailed(0)
                        .actualCount(0)
                        .notificationDetails(new ArrayList<>())
                        .message("No parent email addresses found for selected students")
                        .success(true)
                        .build();
            }

            // Send emails asynchronously
            CompletableFuture<Integer> emailResult = asyncEmailService
                    .sendBulkHealthEventEmailsAsyncWithResult(emailNotifications);
            Integer successfulEmails = emailResult.get(30, TimeUnit.SECONDS); // Wait up to 30 seconds

            return HealthEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle("Sự kiện y tế")
                    .studentName(healthEvent.getStudent() != null ? healthEvent.getStudent().getFullName() : "Không xác định")
                    .problem(healthEvent.getProblem())
                    .description(healthEvent.getDescription())
                    .solution(healthEvent.getSolution())
                    .extent(healthEvent.getExtent())
                    .eventDate(healthEvent.getEventTime() != null ? healthEvent.getEventTime().toString() : "Không xác định")
                    .eventLocation(healthEvent.getLocation() != null ? healthEvent.getLocation() : "Trường học")
                    .totalParentsNotified(totalParentsFound)
                    .totalEmailsSent(successfulEmails)
                    .totalEmailsFailed(totalParentsFound - successfulEmails)
                    .actualCount(successfulEmails)
                    .notificationDetails(emailNotifications)
                    .message("Selective health event emails sent successfully")
                    .success(true)
                    .build();

        } catch (TimeoutException e) {
            log.error("Timeout while sending selective health event emails for event ID: {}", eventId, e);
            return HealthEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle("Sự kiện y tế")
                    .studentName(healthEvent != null && healthEvent.getStudent() != null ? healthEvent.getStudent().getFullName() : "Không xác định")
                    .problem(healthEvent != null ? healthEvent.getProblem() : "Không xác định")
                    .description(healthEvent != null ? healthEvent.getDescription() : "Không có mô tả")
                    .solution(healthEvent != null ? healthEvent.getSolution() : "Không có giải pháp")
                    .extent(healthEvent != null ? healthEvent.getExtent() : "NORMAL")
                    .eventDate(healthEvent != null && healthEvent.getEventTime() != null ? healthEvent.getEventTime().toString() : "Không xác định")
                    .eventLocation(healthEvent != null ? healthEvent.getLocation() : "Trường học")
                    .totalParentsNotified(0)
                    .totalEmailsSent(0)
                    .totalEmailsFailed(0)
                    .actualCount(0)
                    .notificationDetails(new ArrayList<>())
                    .message("Email sending timed out. Some emails may still be processing.")
                    .success(false)
                    .build();
        } catch (Exception e) {
            log.error("Error sending selective health event emails for event ID: {}", eventId, e);
            return HealthEventEmailNotificationDTO.builder()
                    .eventId(eventId)
                    .eventTitle("Sự kiện y tế")
                    .studentName(healthEvent != null && healthEvent.getStudent() != null ? healthEvent.getStudent().getFullName() : "Không xác định")
                    .problem(healthEvent != null ? healthEvent.getProblem() : "Không xác định")
                    .description(healthEvent != null ? healthEvent.getDescription() : "Không có mô tả")
                    .solution(healthEvent != null ? healthEvent.getSolution() : "Không có giải pháp")
                    .extent(healthEvent != null ? healthEvent.getExtent() : "NORMAL")
                    .eventDate(healthEvent != null && healthEvent.getEventTime() != null ? healthEvent.getEventTime().toString() : "Không xác định")
                    .eventLocation(healthEvent != null ? healthEvent.getLocation() : "Trường học")
                    .totalParentsNotified(0)
                    .totalEmailsSent(0)
                    .totalEmailsFailed(0)
                    .actualCount(0)
                    .notificationDetails(new ArrayList<>())
                    .message("Failed to send selective emails: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }
}

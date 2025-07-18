package com.medischool.backend.service.impl.checkup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.EmailNotificationResponseDTO;
import com.medischool.backend.dto.checkup.CheckupEventRequestDTO;
import com.medischool.backend.dto.checkup.CheckupEventResponseStatsDTO;
import com.medischool.backend.dto.checkup.CheckupStatsDTO;
import com.medischool.backend.dto.healthevent.request.SelectiveEmailRequestDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.checkup.CheckupEventCategory;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.checkup.CheckupCategoryRepository;
import com.medischool.backend.repository.checkup.CheckupConsentRepository;
import com.medischool.backend.repository.checkup.CheckupEventCategoryRepository;
import com.medischool.backend.repository.checkup.CheckupEventRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.service.AsyncEmailService;
import com.medischool.backend.service.EmailService;
import com.medischool.backend.service.PdfExportService;
import com.medischool.backend.service.checkup.CheckupEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckupEventServiceImpl implements CheckupEventService {
    private final CheckupEventRepository checkupEventRepository;
    private final CheckupEventCategoryRepository checkupEventCategoryRepository;
    private final CheckupCategoryRepository checkupCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final CheckupConsentRepository checkupConsentRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final EmailService emailService;
    private final AsyncEmailService asyncEmailService;
    private final PdfExportService pdfExportService;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public CheckupStatsDTO getStats() {
        long sent = checkupConsentRepository.count() - checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.NOT_SENT);
        long replied = sent - checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.PENDING);
        long pending = checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.PENDING);
        long categories = checkupCategoryRepository.count();
        return new CheckupStatsDTO(sent, replied, pending, categories);
    }

    @Override
    public CheckupEvent createEvent(String role,CheckupEventRequestDTO requestDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());
        UserProfile createdBy = userProfileRepository.findSingleById(userId);

        CheckupEvent event = CheckupEvent.builder()
                .eventTitle(requestDTO.getEventTitle())
                .schoolYear(requestDTO.getSchoolYear())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        if(role.equalsIgnoreCase("role_nurse")) {
            event.setStatus("PENDING");
        } else if (role.equalsIgnoreCase("role_manager")){
                event.setStatus("APPROVED");
        }

        CheckupEvent savedEvent = checkupEventRepository.save(event);

        for (Long categoryId : requestDTO.getCategoryIds()) {
            CheckupCategory category = checkupCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

            CheckupEventCategory link = CheckupEventCategory.builder()
                    .event(savedEvent)
                    .category(category)
                    .build();

            checkupEventCategoryRepository.save(link);
        }

        return savedEvent;
    }

    private String getStatus(UserProfile profile, String requestStatus) {
        if (profile != null && "MANAGER".equalsIgnoreCase(profile.getRole())) {
            return "APPROVED";
        } else if (requestStatus != null) {
            return requestStatus;
        } else {
            return "PENDING";
        }
    }

    @Override
    public List<CheckupEvent> getAllEvents() {
        return checkupEventRepository.findAll();
    }

    @Override
    public CheckupEvent getEventById(Long id) {
        return checkupEventRepository.findById(id).orElse(null);
    }

    @Override
    public CheckupEvent updateEvent(Long id, CheckupEvent event) {
        event.setId(id);
        return checkupEventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        checkupEventRepository.deleteById(id);
    }

    @Override
    public List<CheckupEvent> getPendingEvent (String status) {
        return checkupEventRepository.findByStatus(status);
    }

    @Override
    public CheckupEvent updateEventStatus(Long eventId, String status, String rejectionReason) {
        String newStatus;
        try {
            newStatus = status;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be one of: APPROVED, REJECTED, PENDING, COMPLETED");
        }

        CheckupEvent event = checkupEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Checkup event not found"));

        event.setStatus(newStatus);

        if (Objects.equals(newStatus, "REJECTED") && rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            event.setRejectionReason(rejectionReason.trim());
        } else if (newStatus != "REJECTED") {
            event.setRejectionReason(null);
        }

        return checkupEventRepository.save(event);
    }

    @Override
    public CheckupEventResponseStatsDTO getEventStats(Long eventId) {
        long totalStudents = studentRepository.count();
        long totalSent = checkupConsentRepository.countByEvent_Id(eventId) - checkupConsentRepository.countByEvent_IdAndConsentStatus(eventId, CheckupConsentStatus.NOT_SENT);
        long totalReplied = totalSent - checkupConsentRepository.countByEvent_IdAndConsentStatus(eventId, CheckupConsentStatus.PENDING);
        long totalNotReplied = totalSent - totalReplied;

        return CheckupEventResponseStatsDTO.builder()
                .totalStudents(totalStudents)
                .totalSent(totalSent)
                .totalReplied(totalReplied)
                .totalNotReplied(totalNotReplied)
                .build();
    }

    @Override
    @Transactional
    public EmailNotificationResponseDTO sendSelectiveHealthCheckupEmailNotifications(Long eventId, List<Long> consentIds,
            SelectiveEmailRequestDTO request) {
        try {
            // Validate event exists
            CheckupEvent checkupEvent = checkupEventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Health checkup event not found with ID: " + eventId));

            // Get health checkup consents for the specified IDs that are still pending (unresponded)
            log.info("Looking for consents with eventId: {} and consentIds: {}", eventId, consentIds);
            List<CheckupEventConsent> consentsToEmail = checkupConsentRepository
                    .findByEventIdAndIdInAndConsentStatus(eventId, consentIds, CheckupConsentStatus.PENDING);

            log.info("Found {} consents to email", consentsToEmail.size());

            if (consentsToEmail.isEmpty()) {
                log.warn("No unresponded consents found for eventId: {} and consentIds: {}", eventId, consentIds);
                return EmailNotificationResponseDTO.builder()
                        .success(true)
                        .message("No unresponded consents found for the selected records")
                        .totalEmailsSent(0)
                        .actualCount(0)
                        .build();
            }

            // Generate PDF report for the health checkup event
            byte[] pdfContent = null;
            try {
                pdfContent = pdfExportService.generateHealthCheckupConsentsPDF(eventId);
                log.info("Successfully generated PDF for health checkup event ID: {}", eventId);
            } catch (Exception e) {
                log.warn("Failed to generate PDF for health checkup event ID: {}. Continuing with email only.", eventId, e);
                // Continue with email sending even if PDF generation fails
            }

            // Prepare email notifications
            List<Map<String, Object>> emailNotifications = new ArrayList<>();
            int emailsSent = 0;
            int emailsFailed = 0;

            for (CheckupEventConsent consent : consentsToEmail) {
                // Lấy parentId từ ParentStudentLink
                List<com.medischool.backend.model.parentstudent.ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudentId(consent.getStudent().getStudentId());
                if (parentLinks.isEmpty()) {
                    log.warn("No parent found for studentId {} (consentId {})", consent.getStudent().getStudentId(), consent.getId());
                    emailsFailed++;
                    continue;
                }
                UUID parentId = parentLinks.get(0).getParentId();
                UserProfile parent = userProfileRepository.findById(parentId).orElse(null);
                if (parent == null || parent.getEmail() == null || parent.getEmail().isEmpty()) {
                    log.warn("No parent email found for studentId {} (consentId {})", consent.getStudent().getStudentId(), consent.getId());
                    emailsFailed++;
                    continue;
                }
                String parentEmail = parent.getEmail();
                String parentName = parent.getFullName();
                String studentName = consent.getStudent().getFullName();

                Map<String, Object> emailData = new HashMap<>();
                emailData.put("toEmail", parentEmail);
                emailData.put("parentName", parentName);
                emailData.put("studentName", studentName);
                emailData.put("eventTitle", checkupEvent.getEventTitle());
                emailData.put("startDate", checkupEvent.getStartDate().toString());
                emailData.put("endDate", checkupEvent.getEndDate().toString());
                emailData.put("consentId", consent.getId());
                String consentUrl = frontendUrl + "/parent/consent?type=checkup&consentId=" + consent.getId();
                emailData.put("consentUrl", consentUrl);
                if (request != null && request.getCustomMessage() != null && !request.getCustomMessage().isEmpty()) {
                    emailData.put("customMessage", request.getCustomMessage());
                }
                if (pdfContent != null) {
                    emailData.put("attachmentContent", pdfContent);
                    emailData.put("attachmentName", "health-checkup-consent-" + eventId + ".pdf");
                    emailData.put("attachmentType", "application/pdf");
                }
                try {
                    // Gửi email đồng bộ với nội dung là một nút bấm và thông tin event, học sinh
                    String emailHtml = "<div style='font-family:sans-serif;font-size:15px;'>"
                        + "<p>Kính gửi Quý phụ huynh,</p>"
                        + "<p>Nhà trường kính mời quý phụ huynh phản hồi phiếu đồng thuận cho học sinh <b>" + studentName + "</b> tham gia sự kiện kiểm tra sức khỏe.</p>"
                        + "<ul style='margin-bottom:16px;'>"
                        + "<li><b>Sự kiện:</b> " + checkupEvent.getEventTitle() + "</li>"
                        + "<li><b>Năm học:</b> " + checkupEvent.getSchoolYear() + "</li>"
                        + "<li><b>Lớp:</b> " + (consent.getStudent().getClassCode() != null ? consent.getStudent().getClassCode() : "") + "</li>"
                        + "<li><b>Ngày bắt đầu:</b> " + checkupEvent.getStartDate() + "</li>"
                        + "<li><b>Ngày kết thúc:</b> " + checkupEvent.getEndDate() + "</li>"
                        + "</ul>"
                        + "<a href='" + consentUrl + "' style='display:inline-block;padding:12px 28px;background:#1976d2;color:#fff;text-decoration:none;border-radius:4px;font-weight:bold;margin-top:18px;font-size:16px;'>Phản hồi consent</a>"
                        + "<p style='margin-top:24px;color:#888;font-size:13px;'>Nếu nút không hoạt động, hãy copy link sau và dán vào trình duyệt: <br>"
                        + "<span style='color:#1976d2'>" + consentUrl + "</span></p>"
                        + "</div>";
                    emailService.sendCustomEmail(parentEmail, checkupEvent.getEventTitle(), emailHtml);
                    emailsSent++;
                } catch (Exception e) {
                    log.error("Failed to send email to {}: {}", parentEmail, e.getMessage());
                    emailsFailed++;
                }
            }

            log.info("Đã gửi xong email: thành công {}, thất bại {}", emailsSent, emailsFailed);

            return EmailNotificationResponseDTO.builder()
                    .success(emailsFailed == 0)
                    .message("Đã gửi xong email: thành công " + emailsSent + ", thất bại " + emailsFailed)
                    .totalEmailsSent(emailsSent)
                    .actualCount(emailsSent)
                    .build();

        } catch (Exception e) {
            log.error("Error sending selective health checkup emails for event ID: {}", eventId, e);
            return EmailNotificationResponseDTO.builder()
                    .success(false)
                    .message("Failed to send selective emails: " + e.getMessage())
                    .totalEmailsSent(0)
                    .actualCount(0)
                    .build();
        }
    }
}
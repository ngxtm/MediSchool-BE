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
                    // Gửi email đồng bộ với format giống hình mẫu
                    String emailHtml = String.format(
                        """
                        <div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;box-shadow:0 4px 24px rgba(0,0,0,0.07);overflow:hidden;font-family:sans-serif;'>
                          <div style='background:linear-gradient(90deg,#023E73 0%%,#1976d2 100%%);color:#fff;text-align:center;padding:32px 24px;'>
                            <img src='cid:logoImage' alt='Logo' style='width:80px;height:80px;object-fit:contain;border-radius:50%%;background:#fff;margin-bottom:12px;' />
                            <h1 style='margin:0;font-size:24px;font-weight:700;letter-spacing:0.5px;'>HỆ THỐNG QUẢN LÝ SỨC KHỎE HỌC SINH</h1>
                            <p style='margin:8px 0 0;font-size:14px;opacity:0.9;'>Chăm sóc sức khỏe toàn diện cho học sinh</p>
                          </div>
                          <div style='background:linear-gradient(90deg,#4facfe 0%%,#00f2fe 100%%);padding:16px 24px;text-align:center;'>
                            <p style='margin:0;color:white;font-size:16px;font-weight:600;'>💉 THÔNG BÁO KHÁM SỨC KHỎE ĐỊNH KỲ</p>
                          </div>
                          <div style='padding:32px 24px;'>
                            <h2 style='color:#023E73;font-size:20px;margin:0 0 16px 0;font-weight:600;'>Kính chào Quý phụ huynh!</h2>
                            <div style='color:#222;font-size:15px;line-height:1.7;'>
                              <p>Nhà trường kính mời quý phụ huynh phản hồi phiếu đồng thuận cho học sinh <b>%s</b> tham gia sự kiện kiểm tra sức khỏe.</p>
                              <ul style='margin-bottom:16px;padding-left:18px;'>
                                <li><b>Sự kiện:</b> %s</li>
                                <li><b>Năm học:</b> %s</li>
                                <li><b>Lớp:</b> %s</li>
                                <li><b>Ngày bắt đầu:</b> %s</li>
                                <li><b>Ngày kết thúc:</b> %s</li>
                              </ul>
                            </div>
                            <div style='text-align:center;margin:32px 0;'>
                              <a href='%s' style='background:#1976d2;color:#fff;padding:16px 32px;font-size:16px;border-radius:50px;text-decoration:none;font-weight:600;display:inline-block;box-shadow:0 4px 15px rgba(25,118,210,0.15);transition:all 0.3s;'>✔️ PHẢN HỒI CONSENT</a>
                            </div>
                            <div style='background:#fffbe6;border:1px solid #ffe58f;border-radius:8px;padding:16px;margin-bottom:24px;'>
                              <p style='margin:0;color:#ad8b00;font-size:13px;line-height:1.5;'>
                                <strong>⚠️ Lưu ý quan trọng:</strong> Vui lòng phản hồi trước ngày kết thúc sự kiện. Nếu có thắc mắc, liên hệ nhà trường để được hỗ trợ.
                              </p>
                            </div>
                          </div>
                          <div style='background:#f6f8fa;color:#888;text-align:center;padding:18px 24px;font-size:13px;'>
                            <div>Liên hệ hỗ trợ: <a href='mailto:medischoolvn@gmail.com' style='color:#1976d2;text-decoration:none;'>medischoolvn@gmail.com</a> | Hotline: 19009999</div>
                            <div style='margin-top:6px;'>© 2024 MediSchool. Email này được gửi tự động, vui lòng không phản hồi.</div>
                          </div>
                        </div>
                        """,
                        studentName,
                        checkupEvent.getEventTitle(),
                        checkupEvent.getSchoolYear(),
                        (consent.getStudent().getClassCode() != null ? consent.getStudent().getClassCode() : ""),
                        checkupEvent.getStartDate(),
                        checkupEvent.getEndDate(),
                        consentUrl
                    );
                    emailService.sendRawHtmlEmail(parentEmail, checkupEvent.getEventTitle(), emailHtml);
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
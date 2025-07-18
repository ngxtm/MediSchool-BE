package com.medischool.backend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.medischool.backend.dto.EmailTemplateRequestDTO;
import com.medischool.backend.dto.EmailTemplateResponseDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.healthevent.HealthEvent;
import com.medischool.backend.model.medication.MedicationRequest;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.checkup.CheckupBasicInfoRepository;
import com.medischool.backend.repository.healthevent.HealthEventRepository;
import com.medischool.backend.repository.medication.MedicationRequestRepository;
import com.medischool.backend.repository.vaccination.VaccineEventRepository;
import com.medischool.backend.service.AsyncEmailService;
import com.medischool.backend.service.EmailTemplateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {
    
    private final AsyncEmailService asyncEmailService;
    private final UserProfileRepository userProfileRepository;
    private final VaccineEventRepository vaccineEventRepository;
    private final HealthEventRepository healthEventRepository;
    private final CheckupBasicInfoRepository checkupBasicInfoRepository;
    private final MedicationRequestRepository medicationRequestRepository;

    @Override
    public EmailTemplateResponseDTO sendVaccinationReminder(EmailTemplateRequestDTO request) {
        try {
            Optional<VaccineEvent> vaccineEventOpt = vaccineEventRepository.findById(Long.parseLong(request.getEventId()));
            if (vaccineEventOpt.isEmpty()) {
                return createErrorResponse("Vaccine event not found");
            }
            
            VaccineEvent vaccineEvent = vaccineEventOpt.get();
            
            List<UserProfile> recipients = getRecipients(request.getRecipientIds());
            if (recipients.isEmpty()) {
                return createErrorResponse("No valid recipients found");
            }

            String subject = "💉 Thông báo tiêm chủng quan trọng";
            String content = generateVaccinationEmailContent(vaccineEvent);

            for (UserProfile recipient : recipients) {
                String personalizedContent = personalizeContent(content, recipient, vaccineEvent);
                Map<String, Object> emailData = Map.of(
                    "to", recipient.getEmail(),
                    "subject", subject,
                    "content", personalizedContent
                );
                asyncEmailService.sendSingleEmailAsync(emailData);
            }

            return createSuccessResponse(recipients.size(), "vaccination_reminder", request.getEventId());
            
        } catch (Exception e) {
            log.error("Error sending vaccination reminder emails", e);
            return createErrorResponse("Error sending vaccination reminder emails: " + e.getMessage());
        }
    }

    @Override
    public EmailTemplateResponseDTO sendHealthCheckupNotification(EmailTemplateRequestDTO request) {
        try {
            Optional<HealthEvent> healthEventOpt = healthEventRepository.findById(Long.parseLong(request.getEventId()));
            if (healthEventOpt.isEmpty()) {
                return createErrorResponse("Health event not found");
            }
            
            HealthEvent healthEvent = healthEventOpt.get();
            
            List<UserProfile> recipients = getRecipients(request.getRecipientIds());
            if (recipients.isEmpty()) {
                return createErrorResponse("No valid recipients found");
            }

            String subject = "🏥 Thông báo kiểm tra sức khỏe";
            String content = generateHealthCheckupEmailContent(healthEvent);

            for (UserProfile recipient : recipients) {
                String personalizedContent = personalizeContent(content, recipient, healthEvent);
                Map<String, Object> emailData = Map.of(
                    "to", recipient.getEmail(),
                    "subject", subject,
                    "content", personalizedContent
                );
                asyncEmailService.sendSingleEmailAsync(emailData);
            }

            return createSuccessResponse(recipients.size(), "health_checkup", request.getEventId());
            
        } catch (Exception e) {
            log.error("Error sending health checkup notification emails", e);
            return createErrorResponse("Error sending health checkup notification emails: " + e.getMessage());
        }
    }

    @Override
    public EmailTemplateResponseDTO sendMedicationReminder(EmailTemplateRequestDTO request) {
        try {
            Optional<MedicationRequest> medicationRequestOpt = medicationRequestRepository.findById(Integer.parseInt(request.getEventId()));
            if (medicationRequestOpt.isEmpty()) {
                return createErrorResponse("Medication request not found");
            }
            
            MedicationRequest medicationRequest = medicationRequestOpt.get();
            
            List<UserProfile> recipients = getRecipients(request.getRecipientIds());
            if (recipients.isEmpty()) {
                return createErrorResponse("No valid recipients found");
            }

            String subject = "💊 Nhắc nhở thuốc quan trọng";
            String content = generateMedicationEmailContent(medicationRequest);

            for (UserProfile recipient : recipients) {
                String personalizedContent = personalizeContent(content, recipient, medicationRequest);
                Map<String, Object> emailData = Map.of(
                    "to", recipient.getEmail(),
                    "subject", subject,
                    "content", personalizedContent
                );
                asyncEmailService.sendSingleEmailAsync(emailData);
            }

            return createSuccessResponse(recipients.size(), "medication_reminder", request.getEventId());
            
        } catch (Exception e) {
            log.error("Error sending medication reminder emails", e);
            return createErrorResponse("Error sending medication reminder emails: " + e.getMessage());
        }
    }

    private List<UserProfile> getRecipients(String[] recipientIds) {
        if (recipientIds == null || recipientIds.length == 0) {
            return userProfileRepository.findAll().stream()
                .filter(user -> user.getIsActive() != null && user.getIsActive() && user.getDeletedAt() == null)
                .toList();
        }
        
        return userProfileRepository.findAllById(
            java.util.Arrays.stream(recipientIds)
                .map(UUID::fromString)
                .toList()
        ).stream()
        .filter(user -> user.getIsActive() != null && user.getIsActive() && user.getDeletedAt() == null)
        .toList();
    }

    private String generateVaccinationEmailContent(VaccineEvent vaccineEvent) {
        return String.format("""
            <p style="font-size:18px;font-weight:500;margin-bottom:16px;">Kính chào Quý phụ huynh!</p>
            <p style="color:#555;font-size:15px;margin-bottom:20px;">Chúng tôi xin gửi đến bạn thông báo về lịch tiêm chủng của con em bạn.</p>
            <div style="background:linear-gradient(135deg,#ffecd2 0%,#fcb69f 100%);border-radius:10px;padding:16px 20px;margin-bottom:16px;border-left:4px solid #e67e22;">
              <strong>👨‍👩‍👧‍👦 Thông tin phụ huynh & học sinh</strong><br/>
              Phụ huynh: <b>{parentName}</b><br/>
              Học sinh: <b>{studentName}</b>
            </div>
            <div style="background:linear-gradient(135deg,#d299c2 0%,#fef9d7 100%);border-radius:10px;padding:16px 20px;margin-bottom:16px;border-left:4px solid #9b59b6;">
              <strong>💉 Thông tin tiêm chủng</strong><br/>
              Loại vaccine: <b>%s</b><br/>
              Thời gian: <b>%s</b><br/>
              Địa điểm: <b>%s</b>
            </div>
            <div style="background:#fff3cd;border:1px solid #ffeaa7;border-radius:8px;padding:12px 16px;margin-bottom:16px;">
              <b>⚠️ Lưu ý quan trọng:</b> Vui lòng xác nhận tham gia và chuẩn bị đầy đủ giấy tờ cần thiết. Trẻ em cần được phụ huynh đưa đến đúng giờ và mang theo sổ tiêm chủng.
            </div>
            <div style="text-align:center;margin:24px 0;">
              <a href="{consentUrl}" style="background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:#fff;padding:12px 28px;font-size:16px;border-radius:40px;text-decoration:none;font-weight:600;display:inline-block;">✅ XÁC NHẬN THAM GIA TIÊM CHỦNG</a>
            </div>
            <div style="background:#f8f9fa;border-radius:8px;padding:14px 18px;margin-top:18px;font-size:14px;">
              <b>📞 Thông tin liên hệ hỗ trợ</b><br/>
              Email: medischoolvn@gmail.com<br/>
              Hotline: 19009999<br/>
              Thời gian hỗ trợ: 7:00 - 17:00 (Thứ 2 - Thứ 6)
            </div>
            """, 
            vaccineEvent.getVaccine().getName(),
            vaccineEvent.getEventDate(),
            vaccineEvent.getLocation()
        );
    }

    private String generateHealthCheckupEmailContent(HealthEvent healthEvent) {
        return String.format("""
            <p style="font-size:18px;font-weight:500;margin-bottom:16px;">Kính chào Quý phụ huynh!</p>
            <p style="color:#555;font-size:15px;margin-bottom:20px;">Chúng tôi xin gửi đến bạn thông báo về lịch kiểm tra sức khỏe của con em bạn.</p>
            <div style="background:linear-gradient(135deg,#e8f5e8 0%,#c8e6c9 100%);border-radius:10px;padding:16px 20px;margin-bottom:16px;border-left:4px solid #4caf50;">
              <strong>👨‍👩‍👧‍👦 Thông tin phụ huynh & học sinh</strong><br/>
              Phụ huynh: <b>{parentName}</b><br/>
              Học sinh: <b>{studentName}</b>
            </div>
            <div style="background:linear-gradient(135deg,#f3e5f5 0%,#e1bee7 100%);border-radius:10px;padding:16px 20px;margin-bottom:16px;border-left:4px solid #9c27b0;">
              <strong>🏥 Thông tin kiểm tra sức khỏe</strong><br/>
              Ngày kiểm tra: <b>%s</b><br/>
              Địa điểm: <b>%s</b><br/>
              Nội dung kiểm tra: <b>%s</b>
            </div>
            <div style="background:#fff3cd;border:1px solid #ffeaa7;border-radius:8px;padding:12px 16px;margin-bottom:16px;">
              <b>⚠️ Lưu ý quan trọng:</b> Vui lòng chuẩn bị cho học sinh tham gia đầy đủ. Học sinh cần ăn sáng nhẹ trước khi kiểm tra và mang theo sổ khám bệnh nếu có.
            </div>
            <div style="text-align:center;margin:24px 0;">
              <a href="{consentUrl}" style="background:linear-gradient(135deg,#4caf50 0%,#2e7d32 100%);color:#fff;padding:12px 28px;font-size:16px;border-radius:40px;text-decoration:none;font-weight:600;display:inline-block;">✅ XÁC NHẬN THAM GIA KIỂM TRA</a>
            </div>
            <div style="background:#f8f9fa;border-radius:8px;padding:14px 18px;margin-top:18px;font-size:14px;">
              <b>📞 Thông tin liên hệ hỗ trợ</b><br/>
              Email: medischoolvn@gmail.com<br/>
              Hotline: 19009999<br/>
              Thời gian hỗ trợ: 7:00 - 17:00 (Thứ 2 - Thứ 6)
            </div>
            """,
            healthEvent.getEventTime(),
            healthEvent.getLocation(),
            healthEvent.getDescription()
        );
    }

    private String generateMedicationEmailContent(MedicationRequest medicationRequest) {
        return String.format("""
            <p style="font-size:18px;font-weight:500;margin-bottom:16px;">Kính chào Quý phụ huynh!</p>
            <p style="color:#555;font-size:15px;margin-bottom:20px;">Chúng tôi xin gửi đến bạn nhắc nhở về việc cung cấp thuốc cho con em bạn.</p>
            <div style="background:linear-gradient(135deg,#fff3e0 0%,#ffe0b2 100%);border-radius:10px;padding:16px 20px;margin-bottom:16px;border-left:4px solid #ff9800;">
              <strong>👨‍👩‍👧‍👦 Thông tin phụ huynh & học sinh</strong><br/>
              Phụ huynh: <b>{parentName}</b><br/>
              Học sinh: <b>{studentName}</b>
            </div>
            <div style="background:linear-gradient(135deg,#fce4ec 0%,#f8bbd9 100%);border-radius:10px;padding:16px 20px;margin-bottom:16px;border-left:4px solid #e91e63;">
              <strong>💊 Thông tin thuốc</strong><br/>
              Lý do: <b>%s</b><br/>
              Thời gian: <b>%s đến %s</b><br/>
              Ghi chú: <b>%s</b>
            </div>
            <div style="background:#fff3cd;border:1px solid #ffeaa7;border-radius:8px;padding:12px 16px;margin-bottom:16px;">
              <b>⚠️ Lưu ý quan trọng:</b> Vui lòng cung cấp thuốc theo đúng chỉ định và hướng dẫn của nhân viên y tế. Đảm bảo học sinh uống thuốc đúng giờ và đúng liều lượng.
            </div>
            <div style="text-align:center;margin:24px 0;">
              <a href="{consentUrl}" style="background:linear-gradient(135deg,#ff9800 0%,#f57c00 100%);color:#fff;padding:12px 28px;font-size:16px;border-radius:40px;text-decoration:none;font-weight:600;display:inline-block;">✅ XÁC NHẬN CUNG CẤP THUỐC</a>
            </div>
            <div style="background:#f8f9fa;border-radius:8px;padding:14px 18px;margin-top:18px;font-size:14px;">
              <b>📞 Thông tin liên hệ hỗ trợ</b><br/>
              Email: medischoolvn@gmail.com<br/>
              Hotline: 19009999<br/>
              Thời gian hỗ trợ: 7:00 - 17:00 (Thứ 2 - Thứ 6)
            </div>
            """,
            medicationRequest.getReason(),
            medicationRequest.getStartDate(),
            medicationRequest.getEndDate(),
            medicationRequest.getNote()
        );
    }

    private String personalizeContent(String content, UserProfile recipient, Object event) {
        String personalizedContent = content;
        
        personalizedContent = personalizedContent.replace("{parentName}", recipient.getFullName());
        personalizedContent = personalizedContent.replace("{studentName}", "Học sinh");
        personalizedContent = personalizedContent.replace("{consentUrl}", "#");
        
        return personalizedContent;
    }

    private EmailTemplateResponseDTO createSuccessResponse(int recipientCount, String templateType, String eventId) {
        EmailTemplateResponseDTO response = new EmailTemplateResponseDTO();
        response.setSuccess(true);
        response.setMessage("Emails sent successfully");
        response.setRecipientCount(recipientCount);
        response.setTemplateType(templateType);
        response.setEventId(eventId);
        return response;
    }

    private EmailTemplateResponseDTO createErrorResponse(String message) {
        EmailTemplateResponseDTO response = new EmailTemplateResponseDTO();
        response.setSuccess(false);
        response.setMessage(message);
        response.setRecipientCount(0);
        return response;
    }
} 
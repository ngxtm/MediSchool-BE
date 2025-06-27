package com.medischool.backend.service.impl;

import com.medischool.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendVaccineConsentNotification(String toEmail, String parentName, String studentName,
                                               String vaccineName, String eventDate, String eventLocation,
                                               String consentUrl) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Thông báo về sự kiện tiêm chủng - " + vaccineName);

            String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; background: #f6f6f6; margin: 0; padding: 0;">
                                   <div style="max-width: 600px; margin: 0 auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 24px;">
                                       <div style="text-align: center; margin-bottom: 24px;">
                                           <img src="cid:logoImage" alt="Logo" style="width: 80px; border-radius: 40px;"/>
                                           <h1 style="color: #AF42A6; font-size: 2rem; margin: 16px 0 0 0;">TRUNG TÂM Y TẾ THÀNH PHỐ NHA TRANG</h1>
                                       </div>
                                       <h2 style="color: #222; font-size: 1.5rem; margin-bottom: 8px;">Thông báo về sự kiện tiêm chủng</h2>
                                       <p style="color: #444; font-size: 1rem; margin-bottom: 16px;">
                                           Kính gửi <b>%s</b>,<br/>
                                           Con của bạn: <b>%s</b><br/>
                                           Vaccine: <b>%s</b><br/>
                                           Thời gian: <b>%s</b><br/>
                                           Địa điểm: <b>%s</b>
                                       </p>
                                       <div style="text-align: center; margin: 24px 0;">
                                           <a href="%s" style="display: inline-block; background: #AF42A6; color: #fff; padding: 12px 32px; border-radius: 24px; text-decoration: none; font-weight: bold;">
                                               Xem & xác nhận đồng ý tiêm chủng
                                           </a>
                                       </div>
                                       <p style="color: #888; font-size: 0.9rem; text-align: center; margin-top: 32px;">
                                           Nếu bạn có thắc mắc, vui lòng liên hệ nhà trường để được hỗ trợ.<br/>
                                           Trân trọng!
                                       </p>
                                   </div>
                               </body>
                    
            </html>
        """, parentName, studentName, vaccineName, eventDate, eventLocation, consentUrl);

            helper.setText(htmlContent, true);

            ClassPathResource image = new ClassPathResource("static/logo.png");
            helper.addInline("logoImage", image);

            mailSender.send(mimeMessage);

            log.info("HTML email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public void sendBulkVaccineConsentNotifications(List<Map<String, Object>> parentNotifications) {
        for (Map<String, Object> notification : parentNotifications) {
            try {
                String toEmail = (String) notification.get("email");
                String parentName = (String) notification.get("parentName");
                String studentName = (String) notification.get("studentName");
                String vaccineName = (String) notification.get("vaccineName");
                String eventDate = (String) notification.get("eventDate");
                String eventLocation = (String) notification.get("eventLocation");
                String consentUrl = (String) notification.get("consentUrl");
                
                sendVaccineConsentNotification(toEmail, parentName, studentName, vaccineName, 
                                             eventDate, eventLocation, consentUrl);
            } catch (Exception e) {
                log.error("Failed to send bulk email notification", e);
            }
        }
    }
} 
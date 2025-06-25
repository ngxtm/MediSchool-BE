package com.medischool.backend.service.impl;

import com.medischool.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Thông báo về sự kiện tiêm chủng - " + vaccineName);
            
            String emailContent = String.format("""
                Kính gửi %s,
                
                Chúng tôi thông báo về sự kiện tiêm chủng sắp diễn ra:
                
                - Tên vaccine: %s
                - Ngày tiêm: %s
                - Địa điểm: %s
                - Học sinh: %s
                
                Vui lòng truy cập link sau để đưa ra quyết định đồng ý hoặc từ chối:
                %s
                
                Trân trọng,
                Ban Y tế học đường
                """, parentName, vaccineName, eventDate, eventLocation, studentName, consentUrl);
            
            message.setText(emailContent);
            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
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
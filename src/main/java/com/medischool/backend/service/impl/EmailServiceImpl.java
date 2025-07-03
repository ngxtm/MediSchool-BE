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
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Thread pool cho việc gửi email song song
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(5);

    @Override
    public void sendVaccineConsentNotification(String toEmail, String parentName, String studentName,
                                               String vaccineName, String eventDate, String eventLocation,
                                               String consentUrl) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject("Thông báo về sự kiện tiêm chủng - " + vaccineName);

                String htmlContent = String.format("""
                <!DOCTYPE html>
                                                      <html lang="vi">
                                                        <body style="margin: 0; padding: 0; background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%); font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                                                          <table align="center" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); overflow: hidden;">
                                                            <tr>
                                                              <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-align: center; padding: 32px 24px;">
                                                                <div style="display: inline-block; background: rgba(255,255,255,0.2); padding: 16px; border-radius: 50%%; margin-bottom: 16px;">
                                                                 <table width="100%%" cellpadding="0" cellspacing="0">
                                                                                      <tr>
                                                                                        <td align="center">
                                                                                          <img src="cid:logoImage" alt="Logo" style="width: 100px; height: 100px; object-fit: contain; border-radius: 50%%; background: white;" />
                                                                                        </td>
                                                                                      </tr>
                                                                                    </table>
                                                                </div>
                                                                <h1 style="margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;">
                                                                  HỆ THỐNG QUẢN LÝ SỨC KHỎE HỌC SINH
                                                                </h1>
                                                                <p style="margin: 8px 0 0; font-size: 14px; opacity: 0.9;">
                                                                  Chăm sóc sức khỏe toàn diện cho học sinh
                                                                </p>
                                                              </td>
                                                            </tr>
                                                            <tr>
                                                              <td style="background: linear-gradient(90deg, #4facfe 0%%, #00f2fe 100%%); padding: 16px 24px; text-align: center;">
                                                                <p style="margin: 0; color: white; font-size: 16px; font-weight: 600;">
                                                                  💉 THÔNG BÁO TIÊM CHỦNG QUAN TRỌNG
                                                                </p>
                                                              </td>
                                                            </tr>
                                                            <tr>
                                                              <td style="padding: 32px 24px;">
                                                                <div style="margin-bottom: 24px;">
                                                                  <h2 style="color: #2c3e50; font-size: 20px; margin: 0 0 8px; font-weight: 600;">
                                                                    Kính chào Quý phụ huynh!
                                                                  </h2>
                                                                  <p style="color: #7f8c8d; font-size: 14px; margin: 0; line-height: 1.5;">
                                                                    Chúng tôi xin gửi đến bạn thông báo về lịch tiêm chủng của con em bạn
                                                                  </p>
                                                                </div>
                                                                <div style="background: linear-gradient(135deg, #ffecd2 0%%, #fcb69f 100%%); border-radius: 12px; padding: 20px; margin-bottom: 24px; border-left: 4px solid #e67e22;">
                                                                  <h3 style="color: #d35400; font-size: 16px; margin: 0 0 12px; font-weight: 600;">
                                                                    👨‍👩‍👧‍👦 Thông tin phụ huynh & học sinh
                                                                  </h3>
                                                                  <div style="color: #8b4513; font-size: 14px; line-height: 1.6;">
                                                                    <p style="margin: 8px 0;"><strong>Phụ huynh:</strong> <span style="color: #d35400;">%s</span></p>
                                                                    <p style="margin: 8px 0;"><strong>Học sinh:</strong> <span style="color: #d35400;">%s</span></p>
                                                                  </div>
                                                                </div>
                                                                <div style="background: linear-gradient(135deg, #d299c2 0%%, #fef9d7 100%%); border-radius: 12px; padding: 20px; margin-bottom: 24px; border-left: 4px solid #9b59b6;">
                                                                  <h3 style="color: #8e44ad; font-size: 16px; margin: 0 0 12px; font-weight: 600;">
                                                                    💉 Thông tin tiêm chủng
                                                                  </h3>
                                                                  <div style="color: #6a1b9a; font-size: 14px; line-height: 1.6;">
                                                                    <p style="margin: 8px 0;"><strong>Loại vaccine:</strong> <span style="color: #8e44ad;">%s</span></p>
                                                                    <p style="margin: 8px 0;"><strong>Thời gian:</strong> <span style="color: #8e44ad;">%s</span></p>
                                                                    <p style="margin: 8px 0;"><strong>Địa điểm:</strong> <span style="color: #8e44ad;">%s</span></p>
                                                                  </div>
                                                                </div>
                                                                <div style="background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                                                                  <p style="margin: 0; color: #856404; font-size: 13px; line-height: 1.5;">
                                                                    <strong>⚠️ Lưu ý quan trọng:</strong> Vui lòng xác nhận tham gia và chuẩn bị đầy đủ giấy tờ cần thiết.\s
                                                                    Trẻ em cần được phụ huynh đưa đến đúng giờ và mang theo sổ tiêm chủng.
                                                                  </p>
                                                                </div>
                                                                <div style="text-align: center; margin: 32px 0;">
                                                                  <a href="%s" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 16px 32px; font-size: 16px; border-radius: 50px; text-decoration: none; display: inline-block; font-weight: 600; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); transition: all 0.3s ease;">
                                                                    ✅ XÁC NHẬN THAM GIA TIÊM CHỦNG
                                                                  </a>
                                                                </div>
                                                                <div style="background: #f8f9fa; border-radius: 8px; padding: 20px; margin-top: 24px;">
                                                                  <h4 style="color: #495057; font-size: 14px; margin: 0 0 12px; font-weight: 600;">
                                                                    📞 Thông tin liên hệ hỗ trợ
                                                                  </h4>
                                                                  <div style="color: #6c757d; font-size: 13px; line-height: 1.6;">
                                                                    <p style="margin: 4px 0;">📧 Email: medischool@gmail.com</p>
                                                                    <p style="margin: 4px 0;">📱 Hotline: 19009999</p>
                                                                    <p style="margin: 4px 0;">🕐 Thời gian hỗ trợ: 7:00 - 17:00 (Thứ 2 - Thứ 6)</p>
                                                                  </div>
                                                                </div>
                                                              </td>
                                                            </tr>
                                                            <tr>
                                                              <td style="background: #34495e; color: #bdc3c7; text-align: center; padding: 24px;">
                                                                <p style="margin: 0 0 8px; font-size: 13px; line-height: 1.5;">
                                                                  Cảm ơn quý phụ huynh đã tin tương và hợp tác cùng nhà trường
                                                                </p>
                                                                <p style="margin: 0; font-size: 12px; opacity: 0.8;">
                                                                  © 2025 Hệ thống quản lý sức khỏe học sinh. Bảo mật thông tin theo luật định.
                                                                </p>
                                                                <div style="margin-top: 12px; font-size: 11px; opacity: 0.7;">
                                                                  ⚠️ Email này được gửi tự động, vui lòng không phản hồi trực tiếp
                                                                </div>
                                                              </td>
                                                            </tr>
                                                          </table>
                                                        </body>
                                                      </html>
                """, parentName, studentName, vaccineName, eventDate, eventLocation, consentUrl);

                helper.setText(htmlContent, true);

                ClassPathResource image = new ClassPathResource("static/logo.png");
                helper.addInline("logoImage", image);

                mailSender.send(mimeMessage);

                log.info("HTML email sent successfully to: {} (attempt {})", toEmail, retryCount + 1);
                return; // Thành công, thoát khỏi vòng lặp retry
                
            } catch (Exception e) {
                retryCount++;
                log.warn("Failed to send HTML email to: {} (attempt {}/{}): {}", 
                        toEmail, retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("Failed to send HTML email to: {} after {} attempts", toEmail, maxRetries, e);
                    throw new RuntimeException("Failed to send email after " + maxRetries + " attempts: " + e.getMessage());
                }
                
                // Đợi một chút trước khi thử lại
                try {
                    Thread.sleep(2000 * retryCount); // Tăng thời gian chờ theo số lần retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Email sending interrupted", ie);
                }
            }
        }
    }

    @Override
    public void sendBulkVaccineConsentNotifications(List<Map<String, Object>> parentNotifications) {
        if (parentNotifications == null || parentNotifications.isEmpty()) {
            log.warn("No notifications to send");
            return;
        }

        log.info("Starting bulk email sending for {} notifications", parentNotifications.size());
        long startTime = System.currentTimeMillis();

        // Tạo danh sách các CompletableFuture để gửi email song song
        List<CompletableFuture<Void>> futures = parentNotifications.stream()
                .map(notification -> CompletableFuture.runAsync(() -> {
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
                }, emailExecutor))
                .toList();

        // Đợi tất cả email được gửi xong
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(60, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            log.info("Bulk email sending completed in {} ms for {} emails", 
                    (endTime - startTime), parentNotifications.size());
        } catch (Exception e) {
            log.error("Timeout or error during bulk email sending", e);
        }
    }

    @Override
    public void sendHealthEventNotification(String toEmail, String parentName, String studentName,
                                           String problem, String description, String solution,
                                           String extent, String eventDate, String eventLocation) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject("Thông báo sự kiện y tế - " + studentName);

                String severityColor = "DANGEROUS".equalsIgnoreCase(extent) ? "#e74c3c" : "#f39c12";
                String severityText = "DANGEROUS".equalsIgnoreCase(extent) ? "NGUY HIỂM" : "BÌNH THƯỜNG";

                String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin: 0; padding: 0; background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%); font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                  <table align="center" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); overflow: hidden;">
                    <tr>
                      <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-align: center; padding: 32px 24px;">
                        <div style="display: inline-block; background: rgba(255,255,255,0.2); padding: 16px; border-radius: 50%%; margin-bottom: 16px;">
                         <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td align="center">
                              <img src="cid:logoImage" alt="Logo" style="width: 100px; height: 100px; object-fit: contain; border-radius: 50%%; background: white;" />
                            </td>
                          </tr>
                        </table>
                        </div>
                        <h1 style="margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;">
                          HỆ THỐNG QUẢN LÝ SỨC KHỎE HỌC SINH
                        </h1>
                        <p style="margin: 8px 0 0; font-size: 14px; opacity: 0.9;">
                          Chăm sóc sức khỏe toàn diện cho học sinh
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background: linear-gradient(90deg, #4facfe 0%%, #00f2fe 100%%); padding: 16px 24px; text-align: center;">
                        <p style="margin: 0; color: white; font-size: 16px; font-weight: 600;">
                          🏥 THÔNG BÁO SỰ KIỆN Y TẾ QUAN TRỌNG
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding: 32px 24px;">
                        <div style="margin-bottom: 24px;">
                          <h2 style="color: #2c3e50; font-size: 20px; margin: 0 0 8px; font-weight: 600;">
                            Kính chào Quý phụ huynh!
                          </h2>
                          <p style="color: #7f8c8d; font-size: 14px; margin: 0; line-height: 1.5;">
                            Chúng tôi xin gửi đến bạn thông báo về sự kiện y tế của con em bạn
                          </p>
                        </div>
                        <div style="background: linear-gradient(135deg, #ffecd2 0%%, #fcb69f 100%%); border-radius: 12px; padding: 20px; margin-bottom: 24px; border-left: 4px solid #e67e22;">
                          <h3 style="color: #d35400; font-size: 16px; margin: 0 0 12px; font-weight: 600;">
                            👨‍👩‍👧‍👦 Thông tin phụ huynh & học sinh
                          </h3>
                          <div style="color: #8b4513; font-size: 14px; line-height: 1.6;">
                            <p style="margin: 8px 0;"><strong>Phụ huynh:</strong> <span style="color: #d35400;">%s</span></p>
                            <p style="margin: 8px 0;"><strong>Học sinh:</strong> <span style="color: #d35400;">%s</span></p>
                          </div>
                        </div>
                        <div style="background: linear-gradient(135deg, #d299c2 0%%, #fef9d7 100%%); border-radius: 12px; padding: 20px; margin-bottom: 24px; border-left: 4px solid #9b59b6;">
                          <h3 style="color: #8e44ad; font-size: 16px; margin: 0 0 12px; font-weight: 600;">
                            🏥 Thông tin sự kiện y tế
                          </h3>
                          <div style="color: #6a1b9a; font-size: 14px; line-height: 1.6;">
                            <p style="margin: 8px 0;"><strong>Vấn đề:</strong> <span style="color: #8e44ad;">%s</span></p>
                            <p style="margin: 8px 0;"><strong>Mô tả:</strong> <span style="color: #8e44ad;">%s</span></p>
                            <p style="margin: 8px 0;"><strong>Giải pháp:</strong> <span style="color: #8e44ad;">%s</span></p>
                            <p style="margin: 8px 0;"><strong>Thời gian:</strong> <span style="color: #8e44ad;">%s</span></p>
                            <p style="margin: 8px 0;"><strong>Địa điểm:</strong> <span style="color: #8e44ad;">%s</span></p>
                          </div>
                        </div>
                        <div style="background: %s; border: 1px solid %s; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                          <p style="margin: 0; color: white; font-size: 14px; font-weight: 600; text-align: center;">
                            ⚠️ MỨC ĐỘ: %s
                          </p>
                        </div>
                        <div style="background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                          <p style="margin: 0; color: #856404; font-size: 13px; line-height: 1.5;">
                            <strong>⚠️ Lưu ý quan trọng:</strong> Vui lòng theo dõi sức khỏe của con em và liên hệ với nhà trường nếu có bất kỳ thay đổi nào.
                          </p>
                        </div>
                        <div style="background: #f8f9fa; border-radius: 8px; padding: 20px; margin-top: 24px;">
                          <h4 style="color: #495057; font-size: 14px; margin: 0 0 12px; font-weight: 600;">
                            📞 Thông tin liên hệ hỗ trợ
                          </h4>
                          <div style="color: #6c757d; font-size: 13px; line-height: 1.6;">
                            <p style="margin: 4px 0;">📧 Email: medischool@gmail.com</p>
                            <p style="margin: 4px 0;">📱 Hotline: 19009999</p>
                            <p style="margin: 4px 0;">🕐 Thời gian hỗ trợ: 7:00 - 17:00 (Thứ 2 - Thứ 6)</p>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td style="background: #34495e; color: #bdc3c7; text-align: center; padding: 24px;">
                        <p style="margin: 0 0 8px; font-size: 13px; line-height: 1.5;">
                          Cảm ơn quý phụ huynh đã tin tương và hợp tác cùng nhà trường
                        </p>
                        <p style="margin: 0; font-size: 12px; opacity: 0.8;">
                          © 2025 Hệ thống quản lý sức khỏe học sinh. Bảo mật thông tin theo luật định.
                        </p>
                        <div style="margin-top: 12px; font-size: 11px; opacity: 0.7;">
                          ⚠️ Email này được gửi tự động, vui lòng không phản hồi trực tiếp
                        </div>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """, parentName, studentName, problem, description, solution, eventDate, eventLocation, severityColor, severityColor, severityText);

                helper.setText(htmlContent, true);

                ClassPathResource image = new ClassPathResource("static/logo.png");
                helper.addInline("logoImage", image);

                mailSender.send(mimeMessage);

                log.info("Health event email sent successfully to: {} (attempt {})", toEmail, retryCount + 1);
                return;
                
            } catch (Exception e) {
                retryCount++;
                log.warn("Failed to send health event email to: {} (attempt {}/{}): {}", 
                        toEmail, retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("Failed to send health event email to: {} after {} attempts", toEmail, maxRetries, e);
                    throw new RuntimeException("Failed to send email after " + maxRetries + " attempts: " + e.getMessage());
                }

                try {
                    Thread.sleep(2000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Email sending interrupted", ie);
                }
            }
        }
    }

    // Phương thức để shutdown thread pool khi ứng dụng dừng
    public void shutdown() {
        emailExecutor.shutdown();
        try {
            if (!emailExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                emailExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            emailExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 
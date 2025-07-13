package com.medischool.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private ActivityType actionType;
    
    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    
    @Column(name = "entity_id")
    private String entityId;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum ActivityType {
        CREATE("Tạo mới"),
        UPDATE("Cập nhật"),
        DELETE("Xóa"),
        LOGIN("Đăng nhập"),
        LOGOUT("Đăng xuất"),
        IMPORT("Nhập dữ liệu"),
        EXPORT("Xuất dữ liệu"),
        SEND_EMAIL("Gửi email"),
        APPROVE("Phê duyệt"),
        REJECT("Từ chối"),
        VIEW("Xem"),
        DOWNLOAD("Tải xuống");
        
        private final String displayName;
        
        ActivityType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum EntityType {
        USER("Người dùng"),
        STUDENT("Học sinh"),
        VACCINE("Vaccine"),
        VACCINATION_EVENT("Sự kiện tiêm chủng"),
        MEDICINE("Thuốc"),
        MEDICATION_REQUEST("Yêu cầu thuốc"),
        HEALTH_CHECKUP("Kiểm tra sức khỏe"),
        EMAIL("Email"),
        SYSTEM("Hệ thống");
        
        private final String displayName;
        
        EntityType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 
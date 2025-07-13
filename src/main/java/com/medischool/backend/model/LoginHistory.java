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
@Table(name = "login_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "login_time")
    private LocalDateTime loginTime;
    
    @Column(name = "logout_time")
    private LocalDateTime logoutTime;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LoginStatus status;
    
    @Column(name = "session_duration")
    private Long sessionDuration; // tính bằng giây
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum LoginStatus {
        SUCCESS("Thành công"),
        FAILED("Thất bại"),
        TIMEOUT("Hết thời gian"),
        BLOCKED("Bị chặn");
        
        private final String displayName;
        
        LoginStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 
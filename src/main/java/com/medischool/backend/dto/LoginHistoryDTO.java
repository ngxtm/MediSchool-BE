package com.medischool.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.medischool.backend.model.LoginHistory;
import com.medischool.backend.model.LoginHistory.LoginStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDTO {
    
    private Long id;
    private UUID userId;
    private String username;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;
    private String userAgent;
    private String location;
    private LoginStatus status;
    private String statusDisplay;
    private Long sessionDuration;
    private String sessionDurationFormatted;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActiveSession;
    
    public static LoginHistoryDTO fromLoginHistory(LoginHistory loginHistory) {
        Long calculatedSessionDuration = calculateSessionDuration(loginHistory);
        boolean isActive = loginHistory.getLogoutTime() == null && loginHistory.getStatus() == LoginStatus.SUCCESS;
        
        return LoginHistoryDTO.builder()
                .id(loginHistory.getId())
                .userId(loginHistory.getUserId())
                .username(loginHistory.getUsername())
                .loginTime(loginHistory.getLoginTime())
                .logoutTime(loginHistory.getLogoutTime())
                .ipAddress(loginHistory.getIpAddress())
                .userAgent(loginHistory.getUserAgent())
                .location(loginHistory.getLocation())
                .status(loginHistory.getStatus())
                .statusDisplay(loginHistory.getStatus() != null ? loginHistory.getStatus().getDisplayName() : "")
                .sessionDuration(calculatedSessionDuration)
                .sessionDurationFormatted(formatDuration(calculatedSessionDuration, isActive))
                .failureReason(loginHistory.getFailureReason())
                .createdAt(loginHistory.getCreatedAt())
                .updatedAt(loginHistory.getUpdatedAt())
                .isActiveSession(isActive)
                .build();
    }
    
    private static Long calculateSessionDuration(LoginHistory loginHistory) {
        if (loginHistory.getLoginTime() == null) {
            return null;
        }
        
        LocalDateTime endTime = loginHistory.getLogoutTime();
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        return java.time.Duration.between(loginHistory.getLoginTime(), endTime).getSeconds();
    }
    
    private static String formatDuration(Long seconds, boolean isActive) {
        if (seconds == null || seconds == 0) {
            return "N/A";
        }
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder result = new StringBuilder();
        if (hours > 0) {
            result.append(String.format("%dh ", hours));
        }
        if (minutes > 0 || hours > 0) {
            result.append(String.format("%dm ", minutes));
        }
        if (secs > 0 && hours == 0) {
            result.append(String.format("%ds", secs));
        }
        
        String duration = result.toString().trim();
        return isActive ? duration + " (đang hoạt động)" : duration;
    }
} 
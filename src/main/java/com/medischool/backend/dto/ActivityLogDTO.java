package com.medischool.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    
    private Long id;
    private UUID userId;
    private String userName;
    private ActivityType actionType;
    private String actionTypeDisplay;
    private EntityType entityType;
    private String entityTypeDisplay;
    private String entityId;
    private String description;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private String timeAgo;
    
    public static ActivityLogDTO fromActivityLog(com.medischool.backend.model.ActivityLog activityLog) {
        return ActivityLogDTO.builder()
                .id(activityLog.getId())
                .userId(activityLog.getUserId())
                .userName(activityLog.getUserName())
                .actionType(activityLog.getActionType())
                .actionTypeDisplay(activityLog.getActionType() != null ? activityLog.getActionType().getDisplayName() : "")
                .entityType(activityLog.getEntityType())
                .entityTypeDisplay(activityLog.getEntityType() != null ? activityLog.getEntityType().getDisplayName() : "")
                .entityId(activityLog.getEntityId())
                .description(activityLog.getDescription())
                .details(activityLog.getDetails())
                .ipAddress(activityLog.getIpAddress())
                .userAgent(activityLog.getUserAgent())
                .createdAt(activityLog.getCreatedAt())
                .timeAgo(formatTimeAgo(activityLog.getCreatedAt()))
                .build();
    }
    
    private static String formatTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(createdAt, now).getSeconds();
        
        if (seconds < 60) {
            return "Vừa xong";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " phút trước";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " giờ trước";
        } else {
            long days = seconds / 86400;
            return days + " ngày trước";
        }
    }
} 
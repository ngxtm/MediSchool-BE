package com.medischool.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.medischool.backend.dto.ActivityLogDTO;
import com.medischool.backend.model.ActivityLog;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;
import com.medischool.backend.repository.ActivityLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {
    
    private final ActivityLogRepository activityLogRepository;
    
    public ActivityLog createActivityLog(UUID userId, String userName, ActivityType actionType, 
                                       EntityType entityType, String entityId, String description, 
                                       String details, String ipAddress, String userAgent) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .details(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.error("Error creating activity log: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public List<ActivityLogDTO> getRecentActivities(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<ActivityLog> activities = activityLogRepository.findRecentActivities(pageable);
            
            return activities.stream()
                    .map(ActivityLogDTO::fromActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent activities: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ActivityLogDTO> getRecentActivitiesWithPagination(int page, int limit) {
        try {
            Pageable pageable = PageRequest.of(page, limit);
            List<ActivityLog> activities = activityLogRepository.findRecentActivities(pageable);
            
            return activities.stream()
                    .map(ActivityLogDTO::fromActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent activities with pagination: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public long getTotalActivityCount() {
        try {
            return activityLogRepository.count();
        } catch (Exception e) {
            log.error("Error getting total activity count: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    public List<ActivityLogDTO> getActivitiesByUser(UUID userId, int limit) {
        try {
            List<ActivityLog> activities = activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            return activities.stream()
                    .limit(limit)
                    .map(ActivityLogDTO::fromActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting activities by user: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ActivityLogDTO> getActivitiesByActionType(ActivityType actionType, int limit) {
        try {
            List<ActivityLog> activities = activityLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType);
            
            return activities.stream()
                    .limit(limit)
                    .map(ActivityLogDTO::fromActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting activities by action type: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ActivityLogDTO> getActivitiesByEntityType(EntityType entityType, int limit) {
        try {
            List<ActivityLog> activities = activityLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType);
            
            return activities.stream()
                    .limit(limit)
                    .map(ActivityLogDTO::fromActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting activities by entity type: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ActivityLogDTO> searchActivities(String keyword, int limit) {
        try {
            List<ActivityLog> activities = activityLogRepository.searchByKeyword(keyword);
            
            return activities.stream()
                    .limit(limit)
                    .map(ActivityLogDTO::fromActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching activities: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public long getActivityCountByUser(UUID userId) {
        try {
            return activityLogRepository.countByUserId(userId);
        } catch (Exception e) {
            log.error("Error getting activity count by user: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    public long getActivityCountByActionType(ActivityType actionType) {
        try {
            return activityLogRepository.countByActionType(actionType);
        } catch (Exception e) {
            log.error("Error getting activity count by action type: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    public long getActivityCountByEntityType(EntityType entityType) {
        try {
            return activityLogRepository.countByEntityType(entityType);
        } catch (Exception e) {
            log.error("Error getting activity count by entity type: {}", e.getMessage(), e);
            return 0;
        }
    }
    

} 
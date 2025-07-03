package com.medischool.backend.dto.healthevent.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventEmailNotificationDTO {
    private Long eventId;
    private String eventTitle;
    private String studentName;
    private String problem;
    private String description;
    private String solution;
    private String extent;
    private String eventDate;
    private int totalParentsNotified;
    private int totalEmailsSent;
    private int totalEmailsFailed;
    private List<Map<String, Object>> notificationDetails;
    private String message;
    private boolean success;
} 
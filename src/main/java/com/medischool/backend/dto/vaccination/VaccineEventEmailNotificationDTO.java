package com.medischool.backend.dto.vaccination;

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
public class VaccineEventEmailNotificationDTO {
    private Long eventId;
    private String eventTitle;
    private String vaccineName;
    private String eventDate;
    private String eventLocation;
    private int totalParentsNotified;
    private int totalEmailsSent;
    private int totalEmailsFailed;
    private List<Map<String, Object>> notificationDetails;
    private String message;
    private boolean success;
} 
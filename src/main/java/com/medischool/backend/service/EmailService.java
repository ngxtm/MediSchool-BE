package com.medischool.backend.service;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void sendVaccineConsentNotification(String toEmail, String parentName, String studentName, 
                                       String vaccineName, String eventDate, String eventLocation, 
                                       String consentUrl);
    
    void sendBulkVaccineConsentNotifications(List<Map<String, Object>> parentNotifications);
} 
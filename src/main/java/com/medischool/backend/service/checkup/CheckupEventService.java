package com.medischool.backend.service.checkup;

import java.util.List;

import com.medischool.backend.dto.EmailNotificationResponseDTO;
import com.medischool.backend.dto.checkup.CheckupEventRequestDTO;
import com.medischool.backend.dto.checkup.CheckupEventResponseStatsDTO;
import com.medischool.backend.dto.checkup.CheckupStatsDTO;
import com.medischool.backend.dto.healthevent.request.SelectiveEmailRequestDTO;
import com.medischool.backend.model.checkup.CheckupEvent;

public interface CheckupEventService {
    CheckupEvent createEvent(String role, CheckupEventRequestDTO dto);
    List<CheckupEvent> getAllEvents();
    CheckupEvent getEventById(Long id);
    CheckupEvent updateEvent(Long id, CheckupEvent event);
    void deleteEvent(Long id);
    List<CheckupEvent> getPendingEvent(String status);
    CheckupEvent updateEventStatus(Long eventId, String status, String rejectionReason);
    CheckupStatsDTO getStats();
    CheckupEventResponseStatsDTO getEventStats(Long eventId);
    
    /**
     * Send selective email notifications to specific health checkup consents
     * 
     * @param eventId    The health checkup event ID
     * @param consentIds List of consent IDs to send emails to
     * @param request    Optional request containing custom message and template
     *                   type
     * @return Email notification result
     */
    EmailNotificationResponseDTO sendSelectiveHealthCheckupEmailNotifications(Long eventId, List<Long> consentIds,
            SelectiveEmailRequestDTO request);
}
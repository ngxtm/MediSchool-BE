package com.medischool.backend.service.checkup;

import com.medischool.backend.dto.checkup.CheckupEventRequestDTO;
import com.medischool.backend.dto.checkup.CheckupEventResponseStatsDTO;
import com.medischool.backend.dto.checkup.CheckupStatsDTO;
import com.medischool.backend.model.checkup.CheckupEvent;

import java.util.List;

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
}
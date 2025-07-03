package com.medischool.backend.service.healthevent;

import java.util.List;

import com.medischool.backend.dto.healthevent.request.HealthEventEmailNotificationDTO;
import com.medischool.backend.dto.healthevent.request.HealthEventRequestDTO;
import com.medischool.backend.dto.healthevent.response.TotalHealthEventStatusResDTO;
import com.medischool.backend.model.healthevent.HealthEvent;

public interface HealthEventService {
    TotalHealthEventStatusResDTO getTotalHealthEventStatusResDTO();
    List<HealthEvent> getAllHealthEvent();
    HealthEvent createHealthEvent(HealthEventRequestDTO requestDTO);
    HealthEvent getHealthEventById(Long id);
    HealthEvent updateHealthEvent(Long id, HealthEventRequestDTO requestDTO);
    void deleteHealthEvent(Long id);
    HealthEventEmailNotificationDTO sendHealthEventEmailNotifications(Long eventId);
    List<HealthEventEmailNotificationDTO> sendAllHealthEventEmailNotifications();
}

package com.medischool.backend.service.healthevent;

import java.util.List;

import com.medischool.backend.dto.healthevent.response.TotalHealthEventStatusResDTO;
import com.medischool.backend.model.healthevent.HealthEvent;

public interface HealthEventService {
    TotalHealthEventStatusResDTO getTotalHealthEventStatusResDTO();
    List<HealthEvent> getAllHealthEvent();
}

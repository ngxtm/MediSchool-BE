package com.medischool.backend.service.checkup;

import com.medischool.backend.model.checkup.CheckupEvent;

import java.util.List;

public interface CheckupEventService {
    CheckupEvent createEvent(CheckupEvent event, List<Long> categoryIds);
    List<CheckupEvent> getAllEvents();
    CheckupEvent getEventById(Long id);
    CheckupEvent updateEvent(Long id, CheckupEvent event);
    void deleteEvent(Long id);
} 
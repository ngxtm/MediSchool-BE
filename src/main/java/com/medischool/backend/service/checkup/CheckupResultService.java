package com.medischool.backend.service.checkup;

import com.medischool.backend.model.checkup.CheckupResult;

import java.util.List;

public interface CheckupResultService {
    boolean isApproved(Long eventId, Integer studentId, Long categoryId);
    void saveResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt);
    void updateResult(Long eventId, Integer studentId, Long categoryId, String resultData);
    void updateResultById(Long resultId, String resultData);
    List<CheckupResult> getResultsForStudentInEvent(Long eventId, Integer studentId);
} 
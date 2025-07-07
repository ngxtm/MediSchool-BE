package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.model.checkup.CheckupResult;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.checkup.CheckupConsent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.repository.checkup.CheckupResultRepository;
import com.medischool.backend.repository.checkup.CheckupEventRepository;
import com.medischool.backend.repository.checkup.CheckupCategoryRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.checkup.CheckupConsentRepository;
import com.medischool.backend.service.checkup.CheckupResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckupResultServiceImpl implements CheckupResultService {
    private final CheckupResultRepository checkupResultRepository;
    private final CheckupEventRepository checkupEventRepository;
    private final CheckupCategoryRepository checkupCategoryRepository;
    private final StudentRepository studentRepository;
    private final CheckupConsentRepository checkupConsentRepository;

    @Override
    public boolean isApproved(Long eventId, Integer studentId, Long categoryId) {
        return checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId)
            .stream()
            .anyMatch(c -> c.getCategory().getId().equals(categoryId)
                && c.getConsentStatus() != null
                && c.getConsentStatus() == ConsentStatus.APPROVE);
    }

    @Override
    public void saveResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt) {
        CheckupResult result = CheckupResult.builder()
            .event(checkupEventRepository.findById(eventId).orElseThrow())
            .student(studentRepository.findById(studentId).orElseThrow())
            .category(checkupCategoryRepository.findById(categoryId).orElseThrow())
            .resultData(resultData)
            .checkedAt(java.time.LocalDateTime.now())
            .build();
        checkupResultRepository.save(result);
    }

    @Override
    public List<CheckupResult> getResultsForStudentInEvent(Long eventId, Integer studentId) {
        return checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
    }

    @Override
    public void updateResult(Long eventId, Integer studentId, Long categoryId, String resultData) {
        List<CheckupResult> results = checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
        CheckupResult result = results.stream()
            .filter(r -> r.getCategory().getId().equals(categoryId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Checkup result not found"));
        
        result.setResultData(resultData);
        result.setCheckedAt(java.time.LocalDateTime.now());
        checkupResultRepository.save(result);
    }

    @Override
    public void updateResultById(Long resultId, String resultData) {
        CheckupResult result = checkupResultRepository.findById(resultId)
            .orElseThrow(() -> new RuntimeException("Checkup result not found with ID: " + resultId));
        
        result.setResultData(resultData);
        result.setCheckedAt(java.time.LocalDateTime.now());
        checkupResultRepository.save(result);
    }
} 
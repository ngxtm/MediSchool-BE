package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.dto.checkup.CheckupConsentDTO;
import com.medischool.backend.model.checkup.*;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.checkup.*;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.checkup.CheckupConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.medischool.backend.model.enums.CheckupEventScope;

@Service
@RequiredArgsConstructor
public class CheckupConsentServiceImpl implements CheckupConsentService {
    private final CheckupConsentRepository checkupConsentRepository;
    private final CheckupEventRepository checkupEventRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final UserProfileRepository userProfileRepository;
    private final CheckupEventCategoryRepository checkupEventCategoryRepository;
    private final CheckupEventClassRepository checkupEventClassRepository;
    private final CheckupCategoryConsentRepository categoryConsentRepository;

    public Map<String, Object> sendConsentsToAllStudents(Long eventId) {
        CheckupEvent event = checkupEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Checkup event not found"));

        List<CheckupEventCategory> eventCategories = checkupEventCategoryRepository.findByEventId(eventId);
        if (eventCategories.isEmpty()) {
            throw new RuntimeException("No categories found for this event");
        }

        List<Student> students = switch (event.getScope()) {
            case SCHOOL -> studentRepository.findAll();
            case GRADE, CLASS -> {
                List<String> allowedClassCodes = checkupEventClassRepository.findByEventId(eventId).stream()
                        .map(ec -> ((CheckupEventClass) ec).getClassCode())
                        .toList();
                yield studentRepository.findByClassCodeIn(allowedClassCodes);
            }
        };

        int createdConsentCount = 0;
        int createdCategoryConsentCount = 0;

        for (Student student : students) {
            UUID parentId = parentStudentLinkRepository.findByStudentId(student.getStudentId()).stream()
                    .findFirst()
                    .map(link -> link.getParentId())
                    .orElse(null);
            if (parentId == null) continue;

            UserProfile parent = userProfileRepository.findById(parentId).orElse(null);
            if (parent == null) continue;

            Optional<CheckupEventConsent> existingConsentOpt =
                    checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, student.getStudentId());

            CheckupEventConsent consent;
            if (existingConsentOpt.isPresent()) {
                consent = existingConsentOpt.get();
            } else {
                consent = CheckupEventConsent.builder()
                        .event(event)
                        .student(student)
                        .parent(parent)
                        .consentStatus(CheckupConsentStatus.PENDING)
                        .note(null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                consent = checkupConsentRepository.save(consent);
                createdConsentCount++;
            }

            for (CheckupEventCategory ec : eventCategories) {
                boolean exists = categoryConsentRepository.existsByConsentAndEventCategory(consent, ec);
                if (!exists) {
                    CheckupCategoryConsent catConsent = CheckupCategoryConsent.builder()
                            .consent(consent)
                            .eventCategory(ec)
                            .categoryConsentStatus(null)
                            .note(null)
                            .build();
                    categoryConsentRepository.save(catConsent);
                    createdCategoryConsentCount++;
                }
            }
        }

        return Map.of(
                "success", true,
                "event_id", eventId,
                "students_count", students.size(),
                "event_consents_created", createdConsentCount,
                "category_consents_created", createdCategoryConsentCount
        );
    }

    @Override
    public List<CheckupConsentDTO> getAllConsentsForEvent(Long eventId) {
        List<CheckupEventConsent> consents = checkupConsentRepository.findByEventId(eventId);
        return consents.stream()
                .map(consent -> new CheckupConsentDTO(consent))
                .collect(Collectors.toList());
    }
}
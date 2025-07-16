package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.dto.checkup.CheckupConsentDTO;
import com.medischool.backend.dto.checkup.CheckupConsentResponseDTO;
import com.medischool.backend.dto.checkup.CheckupResultItemDTO;
import com.medischool.backend.dto.checkup.ConsentReplyResponse;
import com.medischool.backend.model.checkup.*;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.model.enums.ResultStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.checkup.*;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.checkup.CheckupConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    private final CheckupCategoryConsentRepository categoryConsentRepository;
    private final CheckupResultRepository checkupResultRepository;
    private final CheckupResultItemRepository checkupResultItemRepository;

    public Map<String, Object> sendConsentsToAllStudents(Long eventId) {
        CheckupEvent event = checkupEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Checkup event not found"));

        List<CheckupEventCategory> eventCategories = checkupEventCategoryRepository.findByEventId(eventId);
        if (eventCategories.isEmpty()) {
            throw new RuntimeException("No categories found for this event");
        }

        List<Student> students = studentRepository.findAll();

        int createdConsentCount = 0;
        int createdCategoryConsentCount = 0;

        for (Student student : students) {
            Optional<CheckupEventConsent> existingConsentOpt =
                    checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, student.getStudentId());

            CheckupEventConsent consent;
            if (existingConsentOpt.isPresent()) {
                consent = existingConsentOpt.get();
            } else {
                consent = CheckupEventConsent.builder()
                        .event(event)
                        .student(student)
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
                .map(consent -> {
                    List<CheckupCategoryConsent> categoryConsents = categoryConsentRepository.findAllByConsentId(consent.getId());
                    return new CheckupConsentDTO(consent, categoryConsents);
                })
                .toList();
    }

    @Override
    public CheckupConsentDTO getConsentById(Long id) {
        CheckupEventConsent consent = checkupConsentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Checkup consent not found with ID: " + id));
        List<CheckupCategoryConsent> categoryConsents = categoryConsentRepository.findAllByConsentId(consent.getId());
        return new CheckupConsentDTO(consent, categoryConsents);
    }

    @Override
    @Transactional
    public ConsentReplyResponse submitParentConsentReply(Long consentId, CheckupConsentResponseDTO dto) {
        CheckupEventConsent consent = checkupConsentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consent not found"));

        // Gán parent tại thời điểm phản hồi
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID parentId = UUID.fromString(authentication.getName());
        UserProfile parent = userProfileRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
        consent.setParent(parent);

        // Cập nhật trạng thái tổng
        consent.setConsentStatus(dto.getOverallStatus());
        consent.setNote(dto.getNote());
        consent.setUpdatedAt(LocalDateTime.now());
        checkupConsentRepository.save(consent);

        Map<Long, String> categoryReplies = dto.getCategoryReplies();
        List<CheckupCategoryConsent> categoryConsents = categoryConsentRepository.findAllByConsentId(consentId);

        // Cập nhật từng danh mục
        List<CheckupCategoryConsent> updatedCategoryConsents = new ArrayList<>();
        for (CheckupCategoryConsent categoryConsent : categoryConsents) {
            Long categoryId = categoryConsent.getEventCategory().getId();
            if (categoryReplies.containsKey(categoryId)) {
                String reply = categoryReplies.get(categoryId);
                if ("APPROVED".equalsIgnoreCase(reply)) {
                    categoryConsent.setCategoryConsentStatus(CheckupConsentStatus.APPROVED);
                } else if ("REJECTED".equalsIgnoreCase(reply)) {
                    categoryConsent.setCategoryConsentStatus(CheckupConsentStatus.REJECTED);
                }
                updatedCategoryConsents.add(categoryConsent);
            }
        }
        categoryConsentRepository.saveAll(updatedCategoryConsents);

        // Tạo kết quả nếu phụ huynh đồng ý
        List<CheckupResultItemDTO> createdItems = new ArrayList<>();
        if (CheckupConsentStatus.APPROVED.equals(dto.getOverallStatus())) {
            boolean exists = checkupResultRepository.existsByConsentId(consent.getId());
            if (!exists) {
                CheckupResult result = CheckupResult.builder()
                        .consent(consent)
                        .student(consent.getStudent())
                        .event(consent.getEvent())
                        .build();
                result = checkupResultRepository.save(result);

                for (CheckupCategoryConsent categoryConsent : updatedCategoryConsents) {
                    if (categoryConsent.getCategoryConsentStatus() == CheckupConsentStatus.APPROVED) {
                        CheckupResultItem item = new CheckupResultItem();
                        item.setEventCategory(categoryConsent.getEventCategory());
                        item.setResult(result);
                        item.setStatus(ResultStatus.NO_RESULT);
                        item.setCreatedAt(LocalDateTime.now());

                        item = checkupResultItemRepository.save(item);
                        createdItems.add(new CheckupResultItemDTO(item));
                    }
                }
            }
        }

        return new ConsentReplyResponse(
                new CheckupConsentDTO(consent, updatedCategoryConsents),
                createdItems
        );
    }

    @Override
    public List<CheckupConsentDTO> getConsentsByStudentId(Integer studentId) {
        List<CheckupEventConsent> consents = checkupConsentRepository.findByStudent_StudentId(studentId);
        return consents.stream()
                .map(consent -> {
                    List<CheckupCategoryConsent> categoryConsents =
                            categoryConsentRepository.findAllByConsentId(consent.getId());
                    return new CheckupConsentDTO(consent, categoryConsents);
                })
                .toList();
    }
}
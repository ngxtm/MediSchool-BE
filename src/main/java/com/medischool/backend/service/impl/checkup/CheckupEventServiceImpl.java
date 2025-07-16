package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.dto.checkup.CheckupEventRequestDTO;
import com.medischool.backend.dto.checkup.CheckupStatsDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.checkup.CheckupEventCategory;
import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.checkup.*;
import com.medischool.backend.service.checkup.CheckupEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckupEventServiceImpl implements CheckupEventService {
    private final CheckupEventRepository checkupEventRepository;
    private final CheckupEventCategoryRepository checkupEventCategoryRepository;
    private final CheckupCategoryRepository checkupCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final CheckupConsentRepository checkupConsentRepository;

    @Override
    public CheckupStatsDTO getStats() {
        long sent = checkupConsentRepository.count() - checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.NOT_SENT);
        long replied = sent - checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.PENDING);
        long pending = checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.PENDING);
        long categories = checkupConsentRepository.countByConsentStatus(CheckupConsentStatus.REJECTED);
        return new CheckupStatsDTO(sent, replied, pending, categories);
    }

    @Override
    public CheckupEvent createEvent(CheckupEventRequestDTO requestDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());
        UserProfile createdBy = userProfileRepository.findSingleById(userId);

        CheckupEvent event = CheckupEvent.builder()
                .eventTitle(requestDTO.getEventTitle())
                .schoolYear(requestDTO.getSchoolYear())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .status(getStatus(createdBy, requestDTO.getStatus()))
                .build();

        CheckupEvent savedEvent = checkupEventRepository.save(event);

        for (Long categoryId : requestDTO.getCategoryIds()) {
            CheckupCategory category = checkupCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

            CheckupEventCategory link = CheckupEventCategory.builder()
                    .event(savedEvent)
                    .category(category)
                    .build();

            checkupEventCategoryRepository.save(link);
        }

        return savedEvent;
    }

    private String getStatus(UserProfile profile, String requestStatus) {
        if (profile != null && "MANAGER".equalsIgnoreCase(profile.getRole())) {
            return "APPROVED";
        } else if (requestStatus != null) {
            return requestStatus;
        } else {
            return "PENDING";
        }
    }

    @Override
    public List<CheckupEvent> getAllEvents() {
        return checkupEventRepository.findAll();
    }

    @Override
    public CheckupEvent getEventById(Long id) {
        return checkupEventRepository.findById(id).orElse(null);
    }

    @Override
    public CheckupEvent updateEvent(Long id, CheckupEvent event) {
        event.setId(id);
        return checkupEventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        checkupEventRepository.deleteById(id);
    }

    @Override
    public List<CheckupEvent> getPendingEvent (String status) {
        return checkupEventRepository.findByStatus(status);
    }

    @Override
    public CheckupEvent updateEventStatus(Long eventId, String status, String rejectionReason) {
        String newStatus;
        try {
            newStatus = status;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be one of: APPROVED, REJECTED, PENDING, COMPLETED");
        }

        CheckupEvent event = checkupEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Checkup event not found"));

        event.setStatus(newStatus);

        if (Objects.equals(newStatus, "REJECTED") && rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            event.setRejectionReason(rejectionReason.trim());
        } else if (newStatus != "REJECTED") {
            event.setRejectionReason(null);
        }

        return checkupEventRepository.save(event);
    }
} 
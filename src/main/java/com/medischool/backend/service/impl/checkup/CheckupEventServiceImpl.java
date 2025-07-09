package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.dto.checkup.CheckupEventRequestDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.checkup.CheckupEventCategory;
import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.checkup.CheckupEventClass;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.checkup.CheckupEventClassRepository;
import com.medischool.backend.repository.checkup.CheckupEventRepository;
import com.medischool.backend.repository.checkup.CheckupEventCategoryRepository;
import com.medischool.backend.repository.checkup.CheckupCategoryRepository;
import com.medischool.backend.service.checkup.CheckupEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckupEventServiceImpl implements CheckupEventService {
    private final CheckupEventRepository checkupEventRepository;
    private final CheckupEventCategoryRepository checkupEventCategoryRepository;
    private final CheckupCategoryRepository checkupCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final CheckupEventClassRepository checkupEventClassRepository;

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
                .scope(requestDTO.getScope())
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

        for (String classCode : requestDTO.getClassCodes()) {
            CheckupEventClass link = new CheckupEventClass();
            link.setEventId(event.getId());
            link.setClassCode(classCode);
            checkupEventClassRepository.save(link);
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
} 
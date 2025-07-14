package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.checkup.CheckupEventCategory;
import com.medischool.backend.repository.checkup.CheckupCategoryRepository;
import com.medischool.backend.repository.checkup.CheckupEventCategoryRepository;
import com.medischool.backend.repository.checkup.CheckupEventRepository;
import com.medischool.backend.service.checkup.CheckupEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckupEventServiceImpl implements CheckupEventService {
    private final CheckupEventRepository checkupEventRepository;
    private final CheckupEventCategoryRepository checkupEventCategoryRepository;
    private final CheckupCategoryRepository checkupCategoryRepository;

    @Override
    public CheckupEvent createEvent(CheckupEvent event, List<Long> categoryIds) {
        CheckupEvent savedEvent = checkupEventRepository.save(event);
        if (categoryIds != null) {
            for (Long catId : categoryIds) {
                CheckupCategory category = checkupCategoryRepository.findById(catId).orElseThrow();
                CheckupEventCategory eventCategory = CheckupEventCategory.builder()
                        .event(savedEvent)
                        .category(category)
                        .build();
                checkupEventCategoryRepository.save(eventCategory);
            }
        }
        return savedEvent;
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
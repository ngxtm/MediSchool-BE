package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckupEventCategoryRepository extends JpaRepository<CheckupEventCategory, Long> {
    List<CheckupEventCategory> findByEventId(Long eventId);
    int countByEventId(Long eventId);
    void deleteByEventId(Long eventId);
} 
package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupEventCategoryRepository extends JpaRepository<CheckupEventCategory, Long> {
    java.util.List<CheckupEventCategory> findByEvent_Id(Long eventId);
} 
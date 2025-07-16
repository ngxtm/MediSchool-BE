package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckupEventRepository extends JpaRepository<CheckupEvent, Long> {
    List<CheckupEvent> findByStatus(String status);
} 
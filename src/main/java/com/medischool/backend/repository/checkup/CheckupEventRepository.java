package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupEventRepository extends JpaRepository<CheckupEvent, Long> {
} 
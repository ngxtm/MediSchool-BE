package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupCategoryRepository extends JpaRepository<CheckupCategory, Long> {
} 
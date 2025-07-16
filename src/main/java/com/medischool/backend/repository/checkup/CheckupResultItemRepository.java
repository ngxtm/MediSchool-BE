package com.medischool.backend.repository.checkup;

import com.medischool.backend.dto.checkup.CheckupResultItemDTO;
import com.medischool.backend.dto.checkup.CheckupResultUpdateDTO;
import com.medischool.backend.model.checkup.CheckupResultItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckupResultItemRepository extends JpaRepository<CheckupResultItem, Long> {
    List<CheckupResultItem> findByResultId(Long resultId);
}

package com.medischool.backend.repository;

import com.medischool.backend.model.Medication.MedicationRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRequestItemRepository extends JpaRepository<MedicationRequestItem, Integer> {
}

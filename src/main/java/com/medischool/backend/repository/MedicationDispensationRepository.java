package com.medischool.backend.repository;

import com.medischool.backend.model.medication.MedicationDispensation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationDispensationRepository extends JpaRepository<MedicationDispensation, Integer> {
}
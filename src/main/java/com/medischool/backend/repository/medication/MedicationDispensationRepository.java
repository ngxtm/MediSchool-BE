package com.medischool.backend.repository.medication;

import com.medischool.backend.model.medication.MedicationDispensation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationDispensationRepository extends JpaRepository<MedicationDispensation, Integer> {
    List<MedicationDispensation> findByRequestRequestId(Integer requestId);
}
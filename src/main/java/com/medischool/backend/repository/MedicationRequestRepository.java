package com.medischool.backend.repository;

import com.medischool.backend.model.Medication.MedicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Integer> {
    List<MedicationRequest> findByStudentStudentId(Integer studentId);
}
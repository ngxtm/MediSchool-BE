package com.medischool.backend.repository;

import com.medischool.backend.model.MedicationDispensation;
import com.medischool.backend.model.MedicationRequest;
import com.medischool.backend.model.MedicationRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Integer> {
    List<MedicationRequest> findByStudentStudentId(Integer studentId);
}
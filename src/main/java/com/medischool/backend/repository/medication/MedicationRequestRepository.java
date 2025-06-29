package com.medischool.backend.repository.medication;

import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Integer> {
    List<MedicationRequest> findByStudentStudentId(Integer studentId);

    List<MedicationRequest> findByMedicationStatus(MedicationStatus status);
}
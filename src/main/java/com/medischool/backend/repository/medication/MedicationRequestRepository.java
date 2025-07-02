package com.medischool.backend.repository.medication;

import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Integer> {
    List<MedicationRequest> findByStudentStudentId(Integer studentId);

    List<MedicationRequest> findByMedicationStatus(MedicationStatus status);

    long countByMedicationStatus(MedicationStatus medicationStatus);
}
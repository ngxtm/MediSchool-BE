package com.medischool.backend.repository.medication;

import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  MedicationRequestRepository extends JpaRepository<MedicationRequest, Integer> {
    List<MedicationRequest> findByStudentStudentId(Integer studentId);

    List<MedicationRequest> findByMedicationStatus(MedicationStatus status);

    long countByMedicationStatus(MedicationStatus medicationStatus);

    @Query("SELECT r FROM MedicationRequest r " +
            "WHERE LOWER(r.student.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.student.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MedicationRequest> searchByKeyword(@Param("keyword") String keyword);

}
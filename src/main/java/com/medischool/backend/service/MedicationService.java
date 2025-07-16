package com.medischool.backend.service;

//import com.medischool.backend.dto.medication.CheckupStatsDTO;
import com.medischool.backend.dto.medication.MedicationDispensationDTO;
import com.medischool.backend.dto.medication.MedicationRequestDTO;
import com.medischool.backend.dto.medication.MedicationStatsDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface MedicationService {
    List<MedicationRequest> getRequestsByStudent(Integer studentId, UUID parentId) throws AccessDeniedException;

    MedicationRequest createRequest(MedicationRequestDTO dto, UUID parentId) throws AccessDeniedException;

    MedicationRequest approveRequest(int id, UUID userId, String role);

    MedicationRequest rejectRequest(int id, UUID userId, String rejectReason, String role);

    MedicationRequest receiveMedicine(int id);

    MedicationDispensation dispenseMedication(Integer requestId, MedicationDispensationDTO dto, UUID nurseId);

    MedicationRequest markAsDone(Integer requestId);

    List<MedicationRequest> getAllRequests();

    List<MedicationRequest> getRequestsByStatus(MedicationStatus status);

    List<MedicationRequest> searchRequests(String keyword);

    List<MedicationDispensation> getDispensationsByRequestId(Integer requestId);

    MedicationStatsDTO getRequestStats();

    MedicationRequestDTO getRequestDetail(Integer requestId);

    MedicationRequest disableRequest(Integer requestId);

    MedicationRequestDTO updateRequest(Integer id, MedicationRequestDTO dto);
}

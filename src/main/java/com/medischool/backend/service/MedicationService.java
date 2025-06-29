package com.medischool.backend.service;

import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface MedicationService {
    List<MedicationRequest> getRequestsByStudent(Integer studentId, UUID parentId) throws AccessDeniedException;

    MedicationRequest createRequest(MedicationRequest request);

    MedicationRequest approveRequest(int id, UUID nurseId);

    MedicationRequest rejectRequest(int id, UUID nurseId, String reason);

    MedicationRequest resubmitRequest(Integer requestId, MedicationRequest updatedRequestData, UUID parentId);

    MedicationDispensation dispenseMedication(int requestId, MedicationDispensation dispensation);

    MedicationRequest markAsDone(Integer requestId);

    List<MedicationRequest> getAllRequests();

    List<MedicationRequest> getRequestsByStatus(MedicationStatus status);

    List<MedicationRequest> searchRequests(String keyword);

//    MedicationRequestDetailDTO getRequestDetail(Integer requestId, CustomUserDetails user);
}

package com.medischool.backend.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.annotation.LogActivity;
import com.medischool.backend.dto.medication.MedicationDispensationDTO;
import com.medischool.backend.dto.medication.MedicationRequestDTO;
import com.medischool.backend.dto.medication.MedicationStatsDTO;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;
import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;
import com.medischool.backend.service.MedicationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medication-requests")
class MedicationController {
    @Autowired
    private MedicationService service;

    @GetMapping("/stats")
    public ResponseEntity<MedicationStatsDTO> getRequestStats() {
        MedicationStatsDTO stats = service.getRequestStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pending/count")
    public ResponseEntity<Long> getPendingCount() {
        long count = service.getRequestsByStatus(MedicationStatus.PENDING).size();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/all")
//    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getPendingRequests() {
        return ResponseEntity.ok(service.getRequestsByStatus(MedicationStatus.PENDING));
    }

    //Manager view reviewed requests
    @GetMapping("/reviewed")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<MedicationRequest>> getReviewedRequests() {
        return ResponseEntity.ok(service.getRequestsByStatus(MedicationStatus.REVIEWED));
    }

    //Nurse view approved requests
    @GetMapping("/approved")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getApprovedRequests() {
        List<MedicationRequest> approved = service.getRequestsByStatus(MedicationStatus.APPROVED);
        List<MedicationRequest> dispensing = service.getRequestsByStatus(MedicationStatus.DISPENSING);
        approved.addAll(dispensing);
        return ResponseEntity.ok(approved);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<MedicationRequest>> getRequestsByStudent(
            Authentication authentication,
            @PathVariable Integer studentId
    ) throws AccessDeniedException {
        UUID parentId = UUID.fromString(authentication.getName());
        List<MedicationRequest> requests = service.getRequestsByStudent(studentId, parentId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('PARENT')")
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.MEDICATION_REQUEST,
        description = "Tạo yêu cầu thuốc mới"
    )
    public ResponseEntity<MedicationRequest> createRequest(@RequestBody MedicationRequestDTO dto, Authentication authentication) throws AccessDeniedException {
        UUID parentId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(service.createRequest(dto, parentId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('NURSE') || hasRole('MANAGER')")
    @LogActivity(
        actionType = ActivityType.APPROVE,
        entityType = EntityType.MEDICATION_REQUEST,
        description = "Phê duyệt yêu cầu thuốc",
        entityIdParam = "id"
    )
    public ResponseEntity<MedicationRequest> approve(@PathVariable Integer id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("UNKNOWN");
        return ResponseEntity.ok(service.approveRequest(id, userId, role));
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> receive(@PathVariable Integer id) {
        return ResponseEntity.ok(service.receiveMedicine(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('NURSE') || hasRole('MANAGER')")
    @LogActivity(
        actionType = ActivityType.REJECT,
        entityType = EntityType.MEDICATION_REQUEST,
        description = "Từ chối yêu cầu thuốc",
        entityIdParam = "id"
    )
  
    public ResponseEntity<MedicationRequest> reject(
            @PathVariable Integer id,
            @RequestParam String rejectReason,
            Authentication authentication
    ) {
        UUID nurseId = UUID.fromString(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("UNKNOWN");
        return ResponseEntity.ok(service.rejectRequest(id, nurseId, rejectReason, role));
    }

    @PostMapping("/{requestId}/dispense")
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.MEDICATION_REQUEST,
        description = "Phát thuốc cho yêu cầu",
        entityIdParam = "requestId"
    )
    public ResponseEntity<?> dispenseMedication(
            @PathVariable Integer requestId,
            @RequestBody MedicationDispensationDTO dto,
            Authentication authentication
    ) {
        UUID nurseId = UUID.fromString(authentication.getName());
        service.dispenseMedication(requestId, dto, nurseId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/done")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> markDone(@PathVariable Integer id) {
        return ResponseEntity.ok(service.markAsDone(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicationRequest>> searchRequests(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchRequests(keyword));
    }


    @GetMapping("/dispensations/by-request/{requestId}")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationDispensation>> getDispensationsByRequest(@PathVariable Integer requestId) {
        List<MedicationDispensation> result = service.getDispensationsByRequestId(requestId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}")
    public ResponseEntity<MedicationRequestDTO> getMedicationRequestDetail(@PathVariable Integer id) {
        MedicationRequestDTO dto = service.getRequestDetail(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public MedicationRequestDTO disableRequest(@PathVariable Integer id) {
        service.disableRequest(id);
        return service.getRequestDetail(id);
    }

    @PutMapping("/{id}/update")
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.MEDICATION_REQUEST,
        description = "Cập nhật yêu cầu thuốc",
        entityIdParam = "id"
    )
    public ResponseEntity<MedicationRequestDTO> updateRequest(@PathVariable Integer id, @RequestBody MedicationRequestDTO dto) {
        MedicationRequestDTO updated = service.updateRequest(id, dto);
        return ResponseEntity.ok(updated);
    }
}

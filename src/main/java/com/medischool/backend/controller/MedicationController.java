package com.medischool.backend.controller;

import com.medischool.backend.dto.medication.MedicationDispensationDTO;
import com.medischool.backend.dto.medication.MedicationRequestDTO;
import com.medischool.backend.dto.medication.MedicationStatsDTO;
import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;
import com.medischool.backend.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medication-requests")
class MedicationController {
    @Autowired
    private MedicationService service;

    //stat
    @GetMapping("/stats")
    public ResponseEntity<MedicationStatsDTO> getRequestStats() {
        MedicationStatsDTO stats = service.getRequestStats();
        return ResponseEntity.ok(stats);
    }

    //Nurse view all requests
    @GetMapping("/all")
//    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    //Nurse view pending requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getPendingRequests() {
        return ResponseEntity.ok(service.getRequestsByStatus(MedicationStatus.PENDING));
    }

    //Nurse view approved requests
    @GetMapping("/approved")
//    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getApprovedRequests() {
        List<MedicationRequest> approved = service.getRequestsByStatus(MedicationStatus.APPROVED);
        List<MedicationRequest> dispensing = service.getRequestsByStatus(MedicationStatus.DISPENSING);
        approved.addAll(dispensing);
        return ResponseEntity.ok(approved);
    }

    //Parents view all their requests by student
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

    //Parents create a request
    @PostMapping("/create")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<MedicationRequest> createRequest(@RequestBody MedicationRequestDTO dto, Authentication authentication) throws AccessDeniedException {
        UUID parentId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(service.createRequest(dto, parentId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('NURSE') || hasRole('MANAGER')")
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
    @PreAuthorize("hasRole('NURSE')")
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

    //Nurse dispense medicine
    @PostMapping("/{requestId}/dispense")
    public ResponseEntity<?> dispenseMedication(
            @PathVariable Integer requestId,
            @RequestBody MedicationDispensationDTO dto,
            Authentication authentication
    ) {
        UUID nurseId = UUID.fromString(authentication.getName());
        service.dispenseMedication(dto, nurseId);
        return ResponseEntity.ok().build();
    }

    //Nurse mark done request
    @PutMapping("/{id}/done")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> markDone(@PathVariable Integer id) {
        return ResponseEntity.ok(service.markAsDone(id));
    }

    //Search
    @GetMapping("/search")
    public ResponseEntity<List<MedicationRequest>> searchRequests(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchRequests(keyword));
    }


    //View dispen history
    @GetMapping("/dispensations/by-request/{requestId}")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationDispensation>> getDispensationsByRequest(@PathVariable Integer requestId) {
        List<MedicationDispensation> result = service.getDispensationsByRequestId(requestId);
        return ResponseEntity.ok(result);
    }


    //View detail
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
    public ResponseEntity<MedicationRequestDTO> updateRequest(@PathVariable Integer id, @RequestBody MedicationRequestDTO dto) {
        MedicationRequestDTO updated = service.updateRequest(id, dto);
        return ResponseEntity.ok(updated);
    }
}

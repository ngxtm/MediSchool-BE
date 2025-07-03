package com.medischool.backend.controller;

import com.medischool.backend.dto.medication.MedicationDispensationDTO;
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
        return ResponseEntity.ok(service.getRequestsByStatus(MedicationStatus.APPROVED));
    }

    //Parents view all their requests by student
    @GetMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<MedicationRequest>> getRequestsByStudent(
            Authentication authentication,
            @RequestParam Integer studentId) throws AccessDeniedException {
        UUID parentId = UUID.fromString(authentication.getName());
        List<MedicationRequest> requests =
                service.getRequestsByStudent(studentId, parentId);

        return ResponseEntity.ok(requests);
    }

    //Parents create a request
    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<MedicationRequest> createRequest(@RequestBody MedicationRequest request) {
        return ResponseEntity.ok(service.createRequest(request));
    }

    //Parent resubmit a request
    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<MedicationRequest> resubmitRequest(
            @PathVariable Integer id,
            @RequestBody MedicationRequest updatedData,
            @RequestParam UUID parentId
    ) {
        return ResponseEntity.ok(service.resubmitRequest(id, updatedData, parentId));
    }
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> approve(@PathVariable Integer id, Authentication authentication) {
        UUID nurseId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(service.approveRequest(id, nurseId));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> reject(
            @PathVariable Integer id,
            @RequestParam String reason,
            Authentication authentication
    ) {
        UUID nurseId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(service.rejectRequest(id, nurseId, reason));
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


//    //View detail
//    @GetMapping("/{id}/detail")
//    public MedicationRequestDetailDTO getRequestDetail(@PathVariable Integer id) {
//        return ResponseEntity.ok(service.getRequestDetailWithDispensations(id));
//    }
}

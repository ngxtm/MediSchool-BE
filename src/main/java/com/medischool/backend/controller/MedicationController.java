package com.medischool.backend.controller;

import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;
import com.medischool.backend.service.MedicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medications")
class MedicationController {
    @Autowired
    private MedicationService service;

    //Nurse view all requests
    @GetMapping("/all")
//    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    //Nurse view pending requests
    @GetMapping("/pending")
//    @PreAuthorize("hasRole('NURSE')")
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
//    @PreAuthorize("hasRole('PARENT')")
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

    //Nurse approve request
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> approve(@PathVariable Integer id, @RequestParam UUID nurseId) {
        return ResponseEntity.ok(service.approveRequest(id, nurseId));
    }

    //Nurse reject request
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> reject(
            @PathVariable Integer id,
            @RequestParam UUID nurseId,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(service.rejectRequest(id, nurseId, reason));
    }

    //Nurse dispense medicine
    @PostMapping("/{id}/dispense")
    public ResponseEntity<MedicationDispensation> dispense(@RequestParam Integer requestId, @RequestBody MedicationDispensation disp) {
        return ResponseEntity.ok(service.dispenseMedication(requestId, disp));
    }

    //Nurse mark done request
    @PutMapping("/{id}/done")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<MedicationRequest> markDone(@PathVariable Integer id) {
        return ResponseEntity.ok(service.markAsDone(id));
    }

    //Search
    @GetMapping("/search")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<MedicationRequest>> searchByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchRequests(keyword));
    }

//    //View detail
//    @GetMapping("/{id}/detail")
//    public MedicationRequestDetailDTO getRequestDetail(@PathVariable Integer id) {
//        return ResponseEntity.ok(service.getRequestDetailWithDispensations(id));
//    }
}

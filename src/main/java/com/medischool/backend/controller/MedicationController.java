package com.medischool.backend.controller;

import com.medischool.backend.model.Medication.MedicationDispensation;
import com.medischool.backend.model.Medication.MedicationRequest;
import com.medischool.backend.service.MedicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Medication Controller")
class MedicationController {
    @Autowired
    private MedicationService service;

    @GetMapping
    public ResponseEntity<List<MedicationRequest>> getRequestsByStudent(
            Authentication authentication,
            @RequestParam Integer studentId) throws AccessDeniedException {
        UUID parentId = UUID.fromString(authentication.getName());
        List<MedicationRequest> requests =
                service.getRequestsByStudent(studentId, parentId);

        return ResponseEntity.ok(requests);
    }

    @PostMapping("/medications")
    public ResponseEntity<MedicationRequest> createRequest(@RequestBody MedicationRequest request) {
        return ResponseEntity.ok(service.createRequest(request));
    }

    @PutMapping("/medications/{id}/update")
    public ResponseEntity<MedicationRequest> resubmitRequest(
            @PathVariable Integer id,
            @RequestBody MedicationRequest updatedData,
            @RequestParam UUID parentId
    ) {
        return ResponseEntity.ok(service.resubmitRequest(id, updatedData, parentId));
    }

    @PutMapping("/medications/{id}/approve")
    public ResponseEntity<MedicationRequest> approve(@PathVariable Integer id, @RequestParam UUID nurseId) {
        return ResponseEntity.ok(service.approveRequest(id, nurseId));
    }

    @PutMapping("/medications/{id}/reject")
    public ResponseEntity<MedicationRequest> reject(
            @PathVariable Integer id,
            @RequestParam UUID nurseId,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(service.rejectRequest(id, nurseId, reason));
    }

    @PostMapping("/medications/{id}/dispense")
    public ResponseEntity<MedicationDispensation> dispense(@RequestParam Integer requestId, @RequestBody MedicationDispensation disp) {
        return ResponseEntity.ok(service.dispenseMedication(requestId, disp));
    }

    @PutMapping("/medications")
    public ResponseEntity<MedicationRequest> markDone(@PathVariable Integer id) {
        return ResponseEntity.ok(service.markAsDone(id));
    }
}

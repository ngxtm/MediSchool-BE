package com.medischool.backend.controller;

import com.medischool.backend.model.VaccinationConsent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.service.VaccineConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consents")
@RequiredArgsConstructor
@Tag(name = "Vaccination Consents")
public class VaccineConsentController {
    private final VaccineConsentService consentService;

    @PutMapping("/{consentId}/status")
    @Operation(summary = "Update consent status")
    public ResponseEntity<?> updateConsentStatus(
            @PathVariable Long consentId,
            @RequestParam ConsentStatus status
    ) {
        try {
            VaccinationConsent updated = consentService.updateConsentStatus(consentId, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all consents for a specific student")
    public ResponseEntity<List<VaccinationConsent>> getStudentConsents(
            @PathVariable Integer studentId
    ) {
        List<VaccinationConsent> consents = consentService.getConsentsByStudentId(studentId);
        return ResponseEntity.ok(consents);
    }
}
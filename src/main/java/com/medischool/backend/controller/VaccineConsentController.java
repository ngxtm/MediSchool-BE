package com.medischool.backend.controller;

import com.medischool.backend.dto.vaccine.VaccineConsentInEvent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.service.VaccinationConsentService;
import com.medischool.backend.service.VaccineConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccine-consents")
@RequiredArgsConstructor
@Tag(name = "Vaccination Consents", description = "Vaccine consents endpoints")
public class VaccineConsentController {
    private final VaccineConsentService consentService;
    private final VaccinationConsentService vaccinationConsentService;

    @PutMapping("/{consentId}/status")
    @Operation(summary = "Update consent status")
    public ResponseEntity<?> updateConsentStatus(
            @PathVariable Long consentId,
            @RequestParam ConsentStatus status
    ) {
        try {
            com.medischool.backend.model.vaccine.VaccinationConsent updated = consentService.updateConsentStatus(consentId, status);
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

    @GetMapping("/event/{eventId}/results")
    @Operation(summary = "Get consent results count for an event")
    public ResponseEntity<Map<String, Object>> getConsentResultsByEvent(@PathVariable Long eventId) {
        try {
            Map<String, Object> results = consentService.getConsentResultsByEvent(eventId);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Long> getTotalVaccinationConsent() {
        return ResponseEntity.ok(vaccinationConsentService.getTotalConsents());
    }

    @GetMapping("/vaccine-events/{id}/students")
    @Operation(summary = "Get all vaccine consents of a specific vaccine event")
    public ResponseEntity<List<VaccineConsentInEvent>> getVaccinationConsentsByEventId(@PathVariable("id") Long eventId) {
        return ResponseEntity.ok(vaccinationConsentService.getVaccinationConsentsByEventId(eventId));
    }
}
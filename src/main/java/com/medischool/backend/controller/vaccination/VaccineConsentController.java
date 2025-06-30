package com.medischool.backend.controller.vaccination;

import com.medischool.backend.dto.vaccination.VaccineConsentDTO;
import com.medischool.backend.dto.vaccination.VaccineConsentInEvent;
import com.medischool.backend.dto.vaccination.UpdateConsentStatusRequest;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import com.medischool.backend.service.vaccination.VaccinationConsentService;
import com.medischool.backend.service.vaccination.VaccineConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @RequestBody UpdateConsentStatusRequest request
    ) {
        try {
            com.medischool.backend.model.vaccine.VaccinationConsent updated = consentService.updateConsentStatus(consentId, request.getStatus(), request.getNote());
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
    @Operation(summary = "Get consent results count for all event")
    public ResponseEntity<Map<String, Object>> getConsentResult() {
        try {
            Map<String, Object> results = vaccinationConsentService.getConsentResult();
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/students")
    @Operation(summary = "Get all vaccine consents of a specific vaccine event")
    public ResponseEntity<List<VaccineConsentInEvent>> getVaccinationConsentsByEventId(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(vaccinationConsentService.getVaccinationConsentsByEventId(eventId));
    }

    @GetMapping("/{consentId}")
    @Operation(summary = "Get detailed consent")
    public ResponseEntity<?> getVaccineConsent(@PathVariable Long consentId) {
        try {
            VaccineConsentDTO dto = vaccinationConsentService.getVaccineConsent(consentId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/detail_list")
    @Operation(summary = "Get all consents with details for a specific student (same as getVaccineConsent but for list)")
    public ResponseEntity<List<VaccineConsentDTO>> getStudentConsentDetails(
            @PathVariable Integer studentId
    ) {
        List<VaccineConsentDTO> consents = vaccinationConsentService.getVaccineConsentsByStudentId(studentId);
        return ResponseEntity.ok(consents);
    }
}
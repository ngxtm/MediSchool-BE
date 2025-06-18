package com.medischool.backend.controller;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.model.VaccinationConsent;
import com.medischool.backend.model.VaccineEvent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.service.VaccineEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vaccine-events")
@RequiredArgsConstructor
@Tag(name = "Vaccine Events")
public class VaccineEventController {
    private final VaccineEventService vaccineEventService;

    @PostMapping
    @Operation(summary = "Create a new vaccine event")
    public ResponseEntity<VaccineEvent> createVaccineEvent(@RequestBody VaccineEventRequestDTO requestDTO) {
        return ResponseEntity.ok(vaccineEventService.createVaccineEvent(requestDTO));
    }

    @GetMapping
    @Operation(summary = "Get all vaccine events")
    public ResponseEntity<List<VaccineEvent>> getAllVaccineEvents() {
        List<VaccineEvent> events = vaccineEventService.getAllVaccineEvents();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{eventId}/send-consents")
    public ResponseEntity<?> sendConsents(@PathVariable Long eventId) {
        return ResponseEntity.ok(vaccineEventService.sendConsentsToUnvaccinatedStudents(eventId));
    }


    @PutMapping("/{eventId}/status")
    @Operation(summary = "Update vaccine event status")
    public ResponseEntity<VaccineEvent> updateEventStatus(
            @PathVariable Long eventId,
            @RequestParam String status
    ) {
        try {
            VaccineEvent updatedEvent = vaccineEventService.updateEventStatus(eventId, status);
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Get vaccine events by year")
    public ResponseEntity<List<VaccineEvent>> getVaccineEventsByYear(@PathVariable int year) {
        try {
            List<VaccineEvent> events = vaccineEventService.getVaccineEventsByYear(year);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
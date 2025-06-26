package com.medischool.backend.controller;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.dto.VaccineEventEmailNotificationDTO;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.service.VaccineEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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

    @PostMapping("/{eventId}/send-email-notifications")
    @Operation(summary = "Send bulk email notifications to parents for vaccine consent")
    public ResponseEntity<VaccineEventEmailNotificationDTO> sendEmailNotifications(@PathVariable Long eventId) {
        try {
            VaccineEventEmailNotificationDTO result = vaccineEventService.sendBulkEmailNotifications(eventId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
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

    @GetMapping("/{id}")
    public ResponseEntity<VaccineEvent> getVaccineEventByEventId(@PathVariable("id") Long eventId) {
        try {
            if (vaccineEventService.getVaccineEventById(eventId).isPresent()) {
                return ResponseEntity.ok(vaccineEventService.getVaccineEventById(eventId).get());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<VaccineEvent>> getUpcomingVaccineEvents() {
        try {
            return ResponseEntity.ok(vaccineEventService.getUpcomingVaccineEvent());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{eventId}/create-vaccination-history")
    public ResponseEntity<?> createVaccinationHistory(@PathVariable Long eventId) {
        int count = vaccineEventService.createVaccinationHistoryForAgreedConsents(eventId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "eventId", eventId,
                "history_created", count
        ));
    }
}
package com.medischool.backend.controller.vaccination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.medischool.backend.dto.vaccination.SelectiveEmailRequestDTO;
import com.medischool.backend.dto.vaccination.VaccineEventEmailNotificationDTO;
import com.medischool.backend.dto.vaccination.VaccineEventRequestDTO;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.service.vaccination.VaccineEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
    @Operation(summary = "Send email notifications to parents for vaccine consent")
    public ResponseEntity<?> sendEmailNotifications(@PathVariable Long eventId) {
        System.out.println("=== BULK EMAIL ENDPOINT HIT ===");
        System.out.println("Event ID: " + eventId);
        System.out.println("Timestamp: " + LocalDateTime.now());
        
        try {
            VaccineEventEmailNotificationDTO result = vaccineEventService.sendBulkEmailNotifications2(eventId);
            System.out.println("=== BULK EMAIL RESULT ===");
            System.out.println("Result: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("=== BULK EMAIL ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi gửi email: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{eventId}/send-selective-emails")
    @Operation(summary = "Send selective email notifications to specific consents")
    public ResponseEntity<?> sendSelectiveEmailNotifications(
            @PathVariable Long eventId,
            @RequestBody SelectiveEmailRequestDTO request) {
        try {
            Map<String, Object> result = vaccineEventService.sendSelectiveEmailNotificationsByConsents(eventId, request.getConsentIds());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi gửi email: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{eventId}/status")
    @Operation(summary = "Update vaccine event status")
    public ResponseEntity<VaccineEvent> updateEventStatus(
            @PathVariable Long eventId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason
    ) {
        try {
            VaccineEvent updatedEvent = vaccineEventService.updateEventStatus(eventId, status, rejectionReason);
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
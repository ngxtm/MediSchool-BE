package com.medischool.backend.controller.healthevent;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.healthevent.request.HealthEventRequestDTO;

import com.medischool.backend.dto.healthevent.request.HealthEventEmailNotificationDTO;

import com.medischool.backend.dto.healthevent.response.HealthEventResponseDTO;

import com.medischool.backend.service.healthevent.HealthEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/health-event")
@RequiredArgsConstructor
@Tag(name = "Health Event Controller", description = "Health event endpoints")
public class HealthEventController {
    private final HealthEventService healthEventService;

    @GetMapping("/statistics")
    @Operation(summary = "Get health event statistics", description = "Retrieve total counts of health events by status")
    public ResponseEntity<?> getHealthEventStatus() {
        try {
            return ResponseEntity.ok(healthEventService.getTotalHealthEventStatusResDTO());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all health event", description = "Retrieve a list of all health event")
    public ResponseEntity<List<HealthEventResponseDTO>> getAllHealthEvent() {
        try {
            List<HealthEventResponseDTO> events = healthEventService.getAllHealthEvent();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create health event", description = "Create a new health event with medicines")
    public ResponseEntity<?> createHealthEvent(@RequestBody HealthEventRequestDTO requestDTO) {
        try {
            return ResponseEntity.ok(healthEventService.createHealthEvent(requestDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get health event by ID", description = "Retrieve a specific health event by its ID")
    public ResponseEntity<?> getHealthEventById(@PathVariable Long id) {
        try {
            HealthEventResponseDTO result = healthEventService.getHealthEventById(id);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update health event", description = "Update an existing health event")
    public ResponseEntity<?> updateHealthEvent(@PathVariable Long id, @RequestBody HealthEventRequestDTO requestDTO) {
        try {
            return ResponseEntity.ok(healthEventService.updateHealthEvent(id, requestDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete health event", description = "Delete a health event and its medicines")
    public ResponseEntity<?> deleteHealthEvent(@PathVariable Long id) {
        try {
            healthEventService.deleteHealthEvent(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{eventId}/send-email-notifications")
    @Operation(summary = "Send email notifications to parents for health event", description = "Send bulk email notifications to parents about a specific health event")
    public ResponseEntity<?> sendEmailNotifications(@PathVariable Long eventId) {
        try {
            HealthEventEmailNotificationDTO result = healthEventService.sendHealthEventEmailNotifications(eventId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/send-all-email-notifications")
    @Operation(summary = "Send email notifications to parents for all health events", description = "Send bulk email notifications to parents about all health events")
    public ResponseEntity<?> sendAllEmailNotifications() {
        try {
            List<HealthEventEmailNotificationDTO> results = healthEventService.sendAllHealthEventEmailNotifications();
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}

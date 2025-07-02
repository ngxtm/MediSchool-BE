package com.medischool.backend.controller.healthevent;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.service.healthevent.HealthEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<?> getAllHealthEvent() {
        try {
            return ResponseEntity.ok(healthEventService.getAllHealthEvent());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

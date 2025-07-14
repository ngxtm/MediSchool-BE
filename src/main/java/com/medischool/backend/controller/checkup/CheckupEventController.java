package com.medischool.backend.controller.checkup;

import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.service.checkup.CheckupEventService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkup-events")
@RequiredArgsConstructor
public class CheckupEventController {
    private final CheckupEventService checkupEventService;

    @PostMapping
    @Operation(summary = "Create a new checkup event")
    public ResponseEntity<CheckupEvent> createEvent(@RequestBody CheckupEvent event) {
        return ResponseEntity.ok(checkupEventService.createEvent(event, event.getCategoryIds()));
    }

    @GetMapping
    @Operation(summary = "Get all checkup events")
    public ResponseEntity<List<CheckupEvent>> getAllEvents() {
        return ResponseEntity.ok(checkupEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checkup event by ID")
    public ResponseEntity<CheckupEvent> getEventById(@PathVariable Long id) {
        CheckupEvent event = checkupEventService.getEventById(id);
        if (event == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update checkup event")
    public ResponseEntity<CheckupEvent> updateEvent(@PathVariable Long id, @RequestBody CheckupEvent event) {
        return ResponseEntity.ok(checkupEventService.updateEvent(id, event));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status of checkup event (Manager approve)")
    public ResponseEntity<CheckupEvent> updateStatus(@PathVariable Long id, @RequestParam String status) {
        CheckupEvent event = checkupEventService.getEventById(id);
        if (event == null) return ResponseEntity.notFound().build();
        event.setStatus(status);
        return ResponseEntity.ok(checkupEventService.updateEvent(id, event));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete checkup event")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        checkupEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
} 
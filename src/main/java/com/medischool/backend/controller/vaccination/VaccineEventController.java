package com.medischool.backend.controller.vaccination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.annotation.LogActivity;
import com.medischool.backend.dto.vaccination.SelectiveEmailRequestDTO;
import com.medischool.backend.dto.vaccination.VaccineEventEmailNotificationDTO;
import com.medischool.backend.dto.vaccination.VaccineEventRequestDTO;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;
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
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.VACCINATION_EVENT,
        description = "Tạo sự kiện tiêm chủng mới: {eventName}"
    )
    public ResponseEntity<VaccineEvent> createVaccineEvent(@RequestBody VaccineEventRequestDTO requestDTO) {
        return ResponseEntity.ok(vaccineEventService.createVaccineEvent(requestDTO));
    }

    @GetMapping
    @Operation(summary = "Get all vaccine events")
    public ResponseEntity<List<VaccineEvent>> getAllVaccineEvents() {
        List<VaccineEvent> events = vaccineEventService.getAllVaccineEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/count")
    @Operation(summary = "Get total count of vaccine events")
    public ResponseEntity<Long> getVaccineEventCount() {
        try {
            long count = vaccineEventService.getVaccineEventCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{eventId}/send-consents")
    public ResponseEntity<?> sendConsents(@PathVariable Long eventId) {
        return ResponseEntity.ok(vaccineEventService.sendConsentsToUnvaccinatedStudents(eventId));
    }

    @PostMapping("/{eventId}/send-email-notifications")
    @Operation(summary = "Send email notifications to parents for vaccine consent")
    @LogActivity(
        actionType = ActivityType.SEND_EMAIL,
        entityType = EntityType.VACCINATION_EVENT,
        description = "Gửi email thông báo cho sự kiện tiêm chủng {eventId}",
        entityIdParam = "eventId"
    )
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
    @LogActivity(
        actionType = ActivityType.SEND_EMAIL,
        entityType = EntityType.VACCINATION_EVENT,
        description = "Gửi email có chọn lọc cho sự kiện tiêm chủng",
        entityIdParam = "eventId"
    )
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
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.VACCINATION_EVENT,
        description = "Cập nhật trạng thái sự kiện tiêm chủng",
        entityIdParam = "eventId"
    )
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

    @GetMapping("/upcoming/count")
    public ResponseEntity<Long> getUpcomingCount() {
        try {
            List<VaccineEvent> upcoming = vaccineEventService.getUpcomingVaccineEvent();
            return ResponseEntity.ok((long) upcoming.size());
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }

    @PostMapping("/{eventId}/create-vaccination-history")
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.VACCINATION_EVENT,
        description = "Tạo lịch sử tiêm chủng cho sự kiện {eventId}, số lượng: {history_created}",
        entityIdParam = "eventId"
    )
    public ResponseEntity<?> createVaccinationHistory(@PathVariable Long eventId) {
        int count = vaccineEventService.createVaccinationHistoryForAgreedConsents(eventId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "eventId", eventId,
                "history_created", count
        ));
    }
}
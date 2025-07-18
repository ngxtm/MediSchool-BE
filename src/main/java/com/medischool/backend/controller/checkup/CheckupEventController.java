package com.medischool.backend.controller.checkup;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.EmailNotificationResponseDTO;
import com.medischool.backend.dto.checkup.CheckupEventRequestDTO;
import com.medischool.backend.dto.checkup.CheckupEventResponseStatsDTO;
import com.medischool.backend.dto.checkup.CheckupStatsDTO;
import com.medischool.backend.dto.healthevent.request.SelectiveEmailRequestDTO;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.service.PdfExportService;
import com.medischool.backend.service.checkup.CheckupEventService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/health-checkup")
@RequiredArgsConstructor
@Slf4j
public class CheckupEventController {
    private final CheckupEventService checkupEventService;
    private final PdfExportService pdfExportService;

    @GetMapping("/stats")
    public ResponseEntity<CheckupStatsDTO> getRequestStats() {
        CheckupStatsDTO stats = checkupEventService.getStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new checkup event")
    public ResponseEntity<CheckupEvent> createEvent(@RequestBody CheckupEventRequestDTO requestDTO) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        UUID uId = UUID.fromString(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("UNKNOWN");
        CheckupEvent created = checkupEventService.createEvent(role, requestDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "Get all checkup events with student stats")
    public ResponseEntity<List<CheckupEvent>> getAllEvents() {
        return ResponseEntity.ok(checkupEventService.getAllEvents());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CheckupEvent>> getPendingEvent() {
        return ResponseEntity.ok(checkupEventService.getPendingEvent("PENDING"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checkup event by ID")
    public ResponseEntity<CheckupEvent> getEventById(@PathVariable Long id) {
        CheckupEvent event = checkupEventService.getEventById(id);
        if (event == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<CheckupEventResponseStatsDTO> getEventStats(@PathVariable Long id) {
        return ResponseEntity.ok(checkupEventService.getEventStats(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update checkup event")
    public ResponseEntity<CheckupEvent> updateEvent(@PathVariable Long id, @RequestBody CheckupEvent event) {
        return ResponseEntity.ok(checkupEventService.updateEvent(id, event));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete checkup event")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        checkupEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{eventId}/status")
    @Operation(summary = "Update health checkup event status")
    public ResponseEntity<CheckupEvent> updateEventStatus(
            @PathVariable Long eventId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason
    ) {
        try {
            CheckupEvent updated = checkupEventService.updateEventStatus(eventId, status, rejectionReason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Send selective email notifications to specific health checkup consents
     * This endpoint allows managers to send emails only to selected consent records
     * 
     * @param eventId The health checkup event ID
     * @param request Request containing array of consent IDs to send emails to
     * @return Response with email sending results
     */
    @PostMapping("/{eventId}/send-selective-emails")
    @Operation(summary = "Send selective email notifications for health checkup event")
    public ResponseEntity<EmailNotificationResponseDTO> sendSelectiveEmailNotifications(
            @PathVariable Long eventId,
            @RequestBody SelectiveEmailRequestDTO request) {

        try {
            // Validate request
            if (request.getConsentIds() == null || request.getConsentIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(EmailNotificationResponseDTO.builder()
                                .success(false)
                                .message("Consent IDs are required")
                                .totalEmailsSent(0)
                                .actualCount(0)
                                .build());
            }

            // Call service to send selective emails
            EmailNotificationResponseDTO result = checkupEventService.sendSelectiveHealthCheckupEmailNotifications(eventId,
                    request.getConsentIds(), request);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(EmailNotificationResponseDTO.builder()
                            .success(false)
                            .message("Invalid request: " + e.getMessage())
                            .totalEmailsSent(0)
                            .actualCount(0)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailNotificationResponseDTO.builder()
                            .success(false)
                            .message("Failed to send selective emails: " + e.getMessage())
                            .totalEmailsSent(0)
                            .actualCount(0)
                            .build());
        }
    }

    /**
     * Export health checkup consents as PDF
     * This endpoint generates a PDF report of all consents for a health checkup event
     * 
     * @param eventId The health checkup event ID
     * @return PDF file as response with proper headers for download
     */
    @GetMapping("/{eventId}/consents/pdf")
    @Operation(summary = "Export health checkup consents as PDF")
    public ResponseEntity<InputStreamResource> exportHealthCheckupConsentsPDF(@PathVariable Long eventId) {
        try {
            // Validate event exists
            CheckupEvent checkupEvent = checkupEventService.getEventById(eventId);
            if (checkupEvent == null) {
                return ResponseEntity.notFound().build();
            }

            // Generate PDF using the PDF service
            byte[] pdfContent = pdfExportService.generateHealthCheckupConsentsPDF(eventId);

            // Set up response headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "health-checkup-consents-" + eventId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            // Create input stream from PDF content
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfContent));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfContent.length)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for health checkup PDF export: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating health checkup PDF for event ID: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 
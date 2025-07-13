package com.medischool.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.EmailTemplateRequestDTO;
import com.medischool.backend.dto.EmailTemplateResponseDTO;
import com.medischool.backend.service.EmailTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/email-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Templates", description = "Email template management endpoints")
public class EmailTemplateController {
    
    private final EmailTemplateService emailTemplateService;

    @PostMapping("/vaccination-reminder")
    @Operation(summary = "Send vaccination reminder emails using template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vaccination reminder emails sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Server error during email sending")
    })
    public ResponseEntity<EmailTemplateResponseDTO> sendVaccinationReminder(@RequestBody EmailTemplateRequestDTO request) {
        try {
            EmailTemplateResponseDTO response = emailTemplateService.sendVaccinationReminder(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error sending vaccination reminder emails", e);
            EmailTemplateResponseDTO errorResponse = new EmailTemplateResponseDTO();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error sending vaccination reminder emails: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/health-checkup")
    @Operation(summary = "Send health checkup notification emails using template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health checkup notification emails sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Server error during email sending")
    })
    public ResponseEntity<EmailTemplateResponseDTO> sendHealthCheckupNotification(@RequestBody EmailTemplateRequestDTO request) {
        try {
            EmailTemplateResponseDTO response = emailTemplateService.sendHealthCheckupNotification(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error sending health checkup notification emails", e);
            EmailTemplateResponseDTO errorResponse = new EmailTemplateResponseDTO();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error sending health checkup notification emails: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/medication-reminder")
    @Operation(summary = "Send medication reminder emails using template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medication reminder emails sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Server error during email sending")
    })
    public ResponseEntity<EmailTemplateResponseDTO> sendMedicationReminder(@RequestBody EmailTemplateRequestDTO request) {
        try {
            EmailTemplateResponseDTO response = emailTemplateService.sendMedicationReminder(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error sending medication reminder emails", e);
            EmailTemplateResponseDTO errorResponse = new EmailTemplateResponseDTO();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error sending medication reminder emails: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 
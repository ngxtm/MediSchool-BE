package com.medischool.backend.controller.checkup;

import com.medischool.backend.model.checkup.CheckupConsent;
import com.medischool.backend.service.checkup.CheckupConsentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.medischool.backend.service.checkup.SendConsentResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkup-consents")
@RequiredArgsConstructor
public class CheckupConsentController {
    private final CheckupConsentService checkupConsentService;

    @GetMapping("/event/{eventId}/student/{studentId}")
    @Operation(summary = "Get consents for student in event")
    public ResponseEntity<List<CheckupConsent>> getConsents(@PathVariable Long eventId, @PathVariable Integer studentId) {
        return ResponseEntity.ok(checkupConsentService.getConsentsForStudentInEvent(eventId, studentId));
    }

    @PostMapping("/event/{eventId}/student/{studentId}")
    @Operation(summary = "Submit consents for student")
    public ResponseEntity<Void> submitConsents(@PathVariable Long eventId, @PathVariable Integer studentId, @RequestBody ConsentRequestBody body) {
        checkupConsentService.submitConsents(eventId, studentId, body.consents, body.fullyRejected);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/event/{eventId}/send-all")
    @Operation(summary = "Send consents to all parents")
    public ResponseEntity<SendConsentResult> sendConsentToAllParents(@PathVariable Long eventId) {
        SendConsentResult result = checkupConsentService.sendConsentToAllParents(eventId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{consentId}")
    @Operation(summary = "Get consent by ID")
    public ResponseEntity<CheckupConsent> getConsentById(@PathVariable Long consentId) {
        return ResponseEntity.ok(checkupConsentService.getConsentById(consentId));
    }

    @PutMapping("/{consentId}/submit")
    @Operation(summary = "Submit consent decision")
    public ResponseEntity<Void> submitConsentById(@PathVariable Long consentId, @RequestBody ParentConsentRequestBody body) {
        checkupConsentService.submitConsentById(consentId, body.consentStatus, body.note);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/event/{eventId}/student/{studentId}/submit-all")
    @Operation(summary = "Submit all consents for student")
    public ResponseEntity<Void> submitAllConsentsForStudent(@PathVariable Long eventId, @PathVariable Integer studentId, @RequestBody ParentConsentRequestBody body) {
        checkupConsentService.submitAllConsentsForStudent(eventId, studentId, body.consentStatus, body.note);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class ConsentRequestBody {
        public List<CheckupConsentService.ConsentRequest> consents;
        public Boolean fullyRejected;
    }

    @Data
    public static class ParentConsentRequestBody {
        public String consentStatus; // "APPROVE", "REJECT"
        public String note; // ghi chú khi từ chối
    }
} 
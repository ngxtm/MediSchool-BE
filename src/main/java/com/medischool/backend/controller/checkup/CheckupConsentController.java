package com.medischool.backend.controller.checkup;

import com.medischool.backend.dto.checkup.CheckupConsentDTO;
import com.medischool.backend.dto.checkup.CheckupConsentResponseDTO;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.service.checkup.CheckupConsentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkup-consents")
@RequiredArgsConstructor
public class CheckupConsentController {

    private final CheckupConsentService checkupConsentService;

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all consents for a student")
    public ResponseEntity<List<CheckupConsentDTO>> getConsentsByStudent(@PathVariable Integer studentId) {
        return ResponseEntity.ok(checkupConsentService.getConsentsByStudentId(studentId));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all consents for an event")
    public ResponseEntity<List<CheckupConsentDTO>> getAllConsentsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(checkupConsentService.getAllConsentsForEvent(eventId));
    }

    @PostMapping("/event/{eventId}/send-all")
    public ResponseEntity<?> sendAll(@PathVariable Long eventId) {
        Map<String, Object> result = checkupConsentService.sendConsentsToAllStudents(eventId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/consent/{consentId}")
    @Operation(summary = "Get consent by ID")
    public ResponseEntity<CheckupConsentDTO> getConsentById(@PathVariable Long consentId) {
        return ResponseEntity.ok(checkupConsentService.getConsentById(consentId));
    }

    @PutMapping("/consent/{id}/reply")
    public ResponseEntity<CheckupConsentDTO> submitParentConsentReply(
            @PathVariable Long id,
            @RequestBody CheckupConsentResponseDTO dto
    ) {
        CheckupConsentDTO response = checkupConsentService.submitParentConsentReply(id, dto);
        return ResponseEntity.ok(response);
    }

//
//    // ✅ Get consents for a student in event
//    @GetMapping("/event/{eventId}/student/{studentId}")
//    @Operation(summary = "Get consents for student in event")
//    public ResponseEntity<List<CheckupEventConsent>> getConsentsForStudent(
//            @PathVariable Long eventId,
//            @PathVariable Integer studentId
//    ) {
//        return ResponseEntity.ok(checkupConsentService.getConsentsForStudentInEvent(eventId, studentId));
//    }
//
//    // ✅ Submit consents for a student
//    @PostMapping("/event/{eventId}/student/{studentId}")
//    @Operation(summary = "Submit consents for student")
//    public ResponseEntity<Void> submitConsents(
//            @PathVariable Long eventId,
//            @PathVariable Integer studentId,
//            @RequestBody ConsentRequestBody body
//    ) {
//        checkupConsentService.submitConsents(eventId, studentId, body.consents, body.fullyRejected);
//        return ResponseEntity.ok().build();
//    }
//
//    // ✅ Send all consents for event
//    @PostMapping("/event/{eventId}/send")
//    @Operation(summary = "Send consents to all parents")
//    public ResponseEntity<Void> sendConsentToAllParents(@PathVariable Long eventId) {
//        checkupConsentService.sendConsentsToAllStudents(eventId);
//        return ResponseEntity.ok().build();
//    }
//
//    // ✅ Submit a single consent decision
//    @PutMapping("/consent/{consentId}/submit")
//    @Operation(summary = "Submit consent decision")
//    public ResponseEntity<Void> submitConsentById(
//            @PathVariable Long consentId,
//            @RequestBody ParentConsentRequestBody body
//    ) {
//        checkupConsentService.submitConsentById(consentId, body.consentStatus, body.note);
//        return ResponseEntity.ok().build();
//    }
//
//    // ✅ Submit all consents for a student in event
//    @PutMapping("/event/{eventId}/student/{studentId}/submit-all")
//    @Operation(summary = "Submit all consents for student")
//    public ResponseEntity<Void> submitAllConsentsForStudent(
//            @PathVariable Long eventId,
//            @PathVariable Integer studentId,
//            @RequestBody ParentConsentRequestBody body
//    ) {
//        checkupConsentService.submitAllConsentsForStudent(eventId, studentId, body.consentStatus, body.note);
//        return ResponseEntity.ok().build();
//    }
//
//    @Data
//    public static class ConsentRequestBody {
//        public List<CheckupConsentService.ConsentRequest> consents;
//        public Boolean fullyRejected;
//    }
//
//    @Data
//    public static class ParentConsentRequestBody {
//        public String consentStatus; // "APPROVE", "REJECT"
//        public String note;
//    }
}

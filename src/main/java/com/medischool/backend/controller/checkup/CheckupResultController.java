package com.medischool.backend.controller.checkup;

import com.medischool.backend.dto.checkup.CheckupResultDTO;
import com.medischool.backend.dto.checkup.CheckupResultItemDTO;
import com.medischool.backend.dto.checkup.CheckupResultUpdateDTO;
import com.medischool.backend.service.checkup.CheckupResultService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkup-results")
@RequiredArgsConstructor
public class CheckupResultController {
    private final CheckupResultService checkupResultService;

//    @PutMapping("/{resultId}")
//    @Operation(summary = "Record checkup result")
//    public ResponseEntity<?> recordResult(
//            @PathVariable Long resultId,
//            @RequestBody CheckupResultRequest body
//    ) {
//        checkupResultService.updateResultById(resultId, body.getResultData());
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/event/{eventId}/student/{studentId}")
//    @Operation(summary = "Get checkup results for student in event")
//    public ResponseEntity<List<?>> getResultsForStudentInEvent(@PathVariable Long eventId, @PathVariable Integer studentId) {
//        return ResponseEntity.ok(checkupResultService.getResultsForStudentInEvent(eventId, studentId));
//    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all results for an event")
    public ResponseEntity<List<CheckupResultDTO>> getResultsByEventId(@PathVariable Long eventId) {
        return ResponseEntity.ok(checkupResultService.getResultsByEventId(eventId));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all results for a student")
    public ResponseEntity<List<CheckupResultDTO>> getResultsByStudentId(@PathVariable Integer studentId) {
        return ResponseEntity.ok(checkupResultService.getResultsByStudentId(studentId));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CheckupResultItemDTO> updateResultItem(
            @PathVariable Long itemId,
            @RequestBody CheckupResultUpdateDTO dto
    ) {
        return ResponseEntity.ok(checkupResultService.updateResultItem(itemId, dto));
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<CheckupResultDTO> getResultDetail(@PathVariable Long resultId) {
        CheckupResultDTO dto = checkupResultService.getResultDetail(resultId);
        return ResponseEntity.ok(dto);
    }

    @Data
    public static class CheckupResultRequest {
        private String resultData;
        private String checkedAt;
    }
//
//    @RestController
//    @RequestMapping("/api/checkup-results")
//    @RequiredArgsConstructor
//    public class CheckupResultController {
//        private final CheckupResultService checkupResultService;
//
//        @PutMapping("/{resultId}")
//        @Operation(summary = "Record checkup result")
//        public ResponseEntity<?> recordResult(
//                @PathVariable Long resultId,
//                @RequestBody CheckupResultRequest body
//        ) {
//            checkupResultService.updateResultById(resultId, body.getResultData());
//            return ResponseEntity.ok().build();
//        }
//
//        @PutMapping("")
//        @Operation(summary = "Upsert checkup result by eventId, studentId, categoryId")
//        public ResponseEntity<?> upsertResult(
//                @RequestParam Long eventId,
//                @RequestParam Integer studentId,
//                @RequestParam Long categoryId,
//                @RequestBody CheckupResultRequest body
//        ) {
//            checkupResultService.upsertResult(eventId, studentId, categoryId, body.getResultData(), body.getCheckedAt());
//            return ResponseEntity.ok().build();
//        }
//
//        @GetMapping("/event/{eventId}/student/{studentId}")
//        @Operation(summary = "Get checkup results for student in event")
//        public ResponseEntity<List<?>> getResultsForStudentInEvent(@PathVariable Long eventId, @PathVariable Integer studentId) {
//            return ResponseEntity.ok(checkupResultService.getResultsForStudentInEvent(eventId, studentId));
//        }
//
//        @Data
//        public static class CheckupResultRequest {
//            private String resultData;
//            private String checkedAt;
//        }
}
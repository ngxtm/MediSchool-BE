package com.medischool.backend.controller;

import com.medischool.backend.dto.VaccinationHistoryRequestDTO;
import com.medischool.backend.dto.VaccinationHistoryUpdateDTO;
import com.medischool.backend.dto.VaccinationHistoryWithStudentDTO;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.service.PdfExportService;
import com.medischool.backend.service.VaccinationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccination-history")
@RequiredArgsConstructor
public class VaccinationHistoryController {
    private final VaccinationHistoryService vaccinationHistoryService;
    private final PdfExportService pdfExportService;

    @PostMapping
    @Operation(summary = "Create a vaccination history record")
    public ResponseEntity<VaccinationHistory> createHistory(@RequestBody VaccinationHistoryRequestDTO dto) {
        VaccinationHistory saved = vaccinationHistoryService.save(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get vaccination history records for an event with student info")
    public ResponseEntity<List<VaccinationHistoryWithStudentDTO>> getByEventId(@PathVariable Long eventId) {
        List<VaccinationHistoryWithStudentDTO> histories = vaccinationHistoryService.findByEventIdWithStudent(eventId);
        return ResponseEntity.ok(histories);
    }
    
    @GetMapping("/event/{eventId}/pdf")
    @Operation(summary = "Export vaccination history records for an event as PDF")
    public ResponseEntity<byte[]> exportEventHistoryAsPdf(@PathVariable Long eventId) {
        List<VaccinationHistoryWithStudentDTO> histories = vaccinationHistoryService.findByEventIdWithStudent(eventId);
        byte[] pdfBytes = pdfExportService.exportVaccinationHistoryByEvent(eventId, histories);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "vaccination-history-event-" + eventId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @GetMapping("/student/{studentId}/by-category")
    @Operation(summary = "Get vaccination history of a student grouped by category")
    public ResponseEntity<Map<String, List<VaccinationHistory>>> getStudentHistoryByCategory(@PathVariable Integer studentId) {
        Map<String, List<VaccinationHistory>> result = vaccinationHistoryService.getStudentHistoryGroupedByCategory(studentId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{studentId}/pdf")
    @Operation(summary = "Export student vaccination history as PDF")
    public ResponseEntity<byte[]> exportStudentHistoryAsPdf(@PathVariable Integer studentId) {
        Map<String, List<VaccinationHistory>> historyByCategory = vaccinationHistoryService.getStudentHistoryGroupedByCategory(studentId);
        byte[] pdfBytes = pdfExportService.exportStudentVaccinationHistory(studentId, historyByCategory);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "student-vaccination-history-" + studentId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

}

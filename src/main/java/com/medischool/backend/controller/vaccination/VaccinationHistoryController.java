package com.medischool.backend.controller.vaccination;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.vaccination.VaccinationHistoryBulkUpdateDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryBulkUpdateResponseDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryRequestDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryUpdateDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryWithStudentDTO;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.service.PdfExportService;
import com.medischool.backend.service.vaccination.VaccinationHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

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

    @PatchMapping("/{historyId}")
    @Operation(summary = "Update a vaccination history record")
    public ResponseEntity<VaccinationHistory> updateHistory(
            @PathVariable Integer historyId, 
            @RequestBody VaccinationHistoryUpdateDTO dto) {
        
        Optional<VaccinationHistory> updatedHistory = vaccinationHistoryService.update(historyId, dto);
        
        if (updatedHistory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(updatedHistory.get());
    }

    @PatchMapping("/bulk")
    @Operation(summary = "Bulk update vaccination history records")
    public ResponseEntity<VaccinationHistoryBulkUpdateResponseDTO> bulkUpdateHistory(
            @RequestBody VaccinationHistoryBulkUpdateDTO bulkUpdateDTO) {
        
        VaccinationHistoryBulkUpdateResponseDTO response = vaccinationHistoryService.bulkUpdate(bulkUpdateDTO);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{historyId}")
    @Operation(summary = "Get a vaccination history record by ID")
    public ResponseEntity<VaccinationHistory> getHistoryById(@PathVariable Integer historyId) {
        Optional<VaccinationHistory> history = vaccinationHistoryService.findById(historyId);
        
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(history.get());
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

    @GetMapping("/completed/count")
    @Operation(summary = "Get count of completed vaccinations")
    public ResponseEntity<Long> getCompletedCount() {
        try {
            // Đếm tất cả vaccination history records (coi như đã hoàn thành)
            long count = vaccinationHistoryService.findAll().size();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }
}

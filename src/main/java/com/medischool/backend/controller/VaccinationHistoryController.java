package com.medischool.backend.controller;

import com.medischool.backend.dto.VaccinationHistoryRequestDTO;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.service.VaccinationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vaccination-history")
@RequiredArgsConstructor
public class VaccinationHistoryController {
    private final VaccinationHistoryService vaccinationHistoryService;

    @PostMapping
    @Operation(summary = "Create a vaccination history record")
    public ResponseEntity<VaccinationHistory> createHistory(@RequestBody VaccinationHistoryRequestDTO dto) {
        VaccinationHistory saved = vaccinationHistoryService.save(dto);
        return ResponseEntity.ok(saved);
    }
}
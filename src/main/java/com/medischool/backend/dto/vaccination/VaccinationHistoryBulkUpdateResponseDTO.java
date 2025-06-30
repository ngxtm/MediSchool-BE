package com.medischool.backend.dto.vaccination;

import java.util.List;

import com.medischool.backend.model.vaccine.VaccinationHistory;

import lombok.Data;

@Data
public class VaccinationHistoryBulkUpdateResponseDTO {
    private int totalUpdated;
    private int totalRequested;
    private List<VaccinationHistory> updatedRecords;
    private List<String> errors;

    public VaccinationHistoryBulkUpdateResponseDTO(
            int totalRequested, 
            List<VaccinationHistory> updatedRecords, 
            List<String> errors) {
        this.totalRequested = totalRequested;
        this.totalUpdated = updatedRecords.size();
        this.updatedRecords = updatedRecords;
        this.errors = errors;
    }
} 
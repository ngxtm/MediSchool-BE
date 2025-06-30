package com.medischool.backend.dto.vaccination;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class VaccinationHistoryBulkUpdateDTO {
    private List<VaccinationHistoryUpdateItem> updates;
    
    @Data
    public static class VaccinationHistoryUpdateItem {
        private Integer historyId;
        private Integer doseNumber;
        private LocalDate vaccinationDate;
        private String location;
        private String note;
        private Boolean abnormal;
        private String followUpNote;
    }
} 
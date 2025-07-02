package com.medischool.backend.dto.vaccination;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class VaccinationHistoryUpdateDTO {
    private Integer historyId;
    private Integer doseNumber;
    private LocalDate vaccinationDate;
    private String location;
    private String note;
    private Boolean abnormal;
    private String followUpNote;
    private UUID updatedBy;
    private LocalDateTime updatedAt;
}
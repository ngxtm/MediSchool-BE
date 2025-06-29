package com.medischool.backend.dto.vaccination;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class VaccinationHistoryUpdateDTO {
    private Integer historyId;
    private Boolean abnormal;
    private String followUpNote;
    private UUID updatedBy;
    private LocalDateTime updatedAt;
}
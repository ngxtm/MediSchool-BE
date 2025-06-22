package com.medischool.backend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequestDTO {
    private Integer requestId;
    private UUID parentId;
    private Integer studentId;

    private String reason;
    private String title;
    private String note;

    private LocalDate startDate;
    private LocalDate endDate;

    private List<MedicationRequestItemDTO> items;
}
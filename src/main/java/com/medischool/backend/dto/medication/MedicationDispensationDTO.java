package com.medischool.backend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDispensationDTO {
    private Integer requestId;
    private UUID nurseId;
    private OffsetDateTime time;
    private String dosageGiven;
    private String note;
    private Boolean isFinalDose;
}
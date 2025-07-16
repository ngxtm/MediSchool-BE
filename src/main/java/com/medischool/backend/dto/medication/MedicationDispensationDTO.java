package com.medischool.backend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MedicationDispensationDTO {
    private Integer requestId;
    private String medicineName;
    private Integer itemId;
    private String dose;
    private String note;
    private String status;
    private OffsetDateTime time;
    private UUID nurseId;
    private String nurseName;
}
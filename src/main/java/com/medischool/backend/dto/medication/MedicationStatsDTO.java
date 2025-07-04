package com.medischool.backend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationStatsDTO {
    private long total;
    private long pending;
    private long approved;
    private long rejected;
}

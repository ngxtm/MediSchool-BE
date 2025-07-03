package com.medischool.backend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequestItemDTO {
    private String medicineName;
    private String unit;
    private String quantity;
    private String dosage;
    private String note;
}

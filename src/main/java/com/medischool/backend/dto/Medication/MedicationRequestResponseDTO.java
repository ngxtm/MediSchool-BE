package com.medischool.backend.dto.Medication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequestResponseDTO {
    private Integer requestId;
    private UUID parentId;
    private Integer studentId;

    private String reason;
    private String title;
    private String note;

    private String status; // PENDING, APPROVED, REJECTED, DONE
    private String rejectReason;

    private UUID reviewedBy;
    private UUID confirmedBy;

    private LocalDate startDate;
    private LocalDate endDate;
    private OffsetDateTime createAt;
    private OffsetDateTime updateAt;

    private List<MedicationRequestItemDTO> items;
}
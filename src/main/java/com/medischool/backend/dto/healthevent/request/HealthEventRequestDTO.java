package com.medischool.backend.dto.healthevent.request;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthEventRequestDTO {
    private Integer studentId;
    private String problem;
    private String description;
    private String solution;
    private String location;
    private String extent;
    private OffsetDateTime eventTime;
    private UUID recordBy;
    private List<EventMedicineDTO> medicines;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EventMedicineDTO {
        private Long medicineId;
        private Integer quantity;
        private String unit;
        private String note;
    }
} 
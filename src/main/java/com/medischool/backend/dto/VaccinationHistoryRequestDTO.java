package com.medischool.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.medischool.backend.model.vaccine.Vaccine;
import lombok.Data;

@Data
public class VaccinationHistoryRequestDTO {
    private Integer studentId;
    private Long eventId;
    private Vaccine vaccine;
    private Integer doseNumber;
    private LocalDate vaccinationDate;
    private String location;
    private String note;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
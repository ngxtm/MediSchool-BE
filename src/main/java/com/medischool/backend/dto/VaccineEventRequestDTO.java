package com.medischool.backend.dto;

import com.medischool.backend.model.enums.EventScope;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VaccineEventRequestDTO {
    private Long vaccineId;
    private String eventTitle;
    private LocalDate eventDate;
    private EventScope eventScope;
    private String location;
    private String status;
}
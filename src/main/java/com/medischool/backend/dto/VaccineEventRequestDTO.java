package com.medischool.backend.dto;

import com.medischool.backend.model.enums.EventScope;
import com.medischool.backend.model.enums.EventStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VaccineEventRequestDTO {
    private Long vaccineId;
    private String eventTitle;
    private LocalDate eventDate;
    private EventScope eventScope;
    private String location;
    private EventStatus status;
    private LocalDateTime createdAt;
    private List<String> classes;
}
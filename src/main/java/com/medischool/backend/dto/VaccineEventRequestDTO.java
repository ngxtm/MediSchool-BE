package com.medischool.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineEventRequestDTO {
    private Long vaccineId;
    private String eventTitle;
    private LocalDateTime eventDate;
    private String eventScope;
    private String location;
    private String status;
}
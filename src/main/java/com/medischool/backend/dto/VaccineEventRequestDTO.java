package com.medischool.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VaccineEventRequestDTO {
    private Long vaccineId;
    private String eventTitle;
    private LocalDateTime eventDate;
    private String eventScope;
    private String location;
    private String status;
}
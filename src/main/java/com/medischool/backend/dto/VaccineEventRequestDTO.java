package com.medischool.backend.dto;

import com.medischool.backend.model.enums.Scope;
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
    private Scope eventScope;
    private String location;
    private String status;
}
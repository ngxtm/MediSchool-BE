package com.medischool.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentResponseDTO {
    private boolean success;
    private int consentsCreated;
    private Long eventId;
    private String message;
}
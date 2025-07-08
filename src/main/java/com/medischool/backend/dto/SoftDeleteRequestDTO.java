package com.medischool.backend.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftDeleteRequestDTO {
    
    private String reason;
    private UUID deletedBy;
    
    public SoftDeleteRequestDTO(String reason) {
        this.reason = reason;
    }
} 
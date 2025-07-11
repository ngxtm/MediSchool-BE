package com.medischool.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResponseDTO {
    private boolean success;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<UserImportDTO> errors;
    private String message;
} 
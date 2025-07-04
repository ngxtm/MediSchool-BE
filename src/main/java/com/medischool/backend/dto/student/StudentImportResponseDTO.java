package com.medischool.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentImportResponseDTO {
    private boolean success;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<StudentImportDTO> errors;
    private String message;
} 
package com.medischool.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentImportDTO {
    private String studentCode;
    private String fullName;
    private String classCode;
    private Integer grade;
    private LocalDate dateOfBirth;
    private String address;
    private String gender;
    private LocalDate enrollmentDate;
    private String emergencyContact;
    private String emergencyPhone;
    private String status;
    private String avatar;
    
    // Validation fields
    private boolean isValid;
    private String errorMessage;
    private int rowNumber;
} 
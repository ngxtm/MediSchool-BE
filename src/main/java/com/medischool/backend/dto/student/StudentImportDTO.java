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

    private String fatherName;
    private String fatherEmail;
    private String fatherPhone;
    private String fatherAddress;
    private LocalDate fatherDateOfBirth;
    private String fatherGender;
    private String fatherJob;
    private String fatherJobPlace;

    private String motherName;
    private String motherEmail;
    private String motherPhone;
    private String motherAddress;
    private LocalDate motherDateOfBirth;
    private String motherGender;
    private String motherJob;
    private String motherJobPlace;
} 
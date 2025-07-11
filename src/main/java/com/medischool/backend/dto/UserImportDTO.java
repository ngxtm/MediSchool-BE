package com.medischool.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportDTO {
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String address;
    private String gender;
    private String dateOfBirth;
    
    private boolean isValid;
    private String errorMessage;
    private int rowNumber;
} 
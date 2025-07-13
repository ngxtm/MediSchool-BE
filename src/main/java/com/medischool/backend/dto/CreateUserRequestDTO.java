package com.medischool.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.medischool.backend.model.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {
    private UUID id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String role;
    private String password;
    private Boolean isActive;
} 
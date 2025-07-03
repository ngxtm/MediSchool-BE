package com.medischool.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.medischool.backend.model.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentResponseDTO {
    private UUID parentId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String job;
    private String jobPlace;
    private String relationship;
} 
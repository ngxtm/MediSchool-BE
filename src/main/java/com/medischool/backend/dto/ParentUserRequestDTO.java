package com.medischool.backend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.medischool.backend.model.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParentUserRequestDTO {
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String password;
    private Boolean isActive;
    private String job;
    private String jobPlace;
} 
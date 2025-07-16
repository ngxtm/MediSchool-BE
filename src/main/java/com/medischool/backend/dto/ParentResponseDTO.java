package com.medischool.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.medischool.backend.model.enums.Gender;
import com.medischool.backend.model.enums.Relationship;

import lombok.*;

@Data
@Builder
@Getter
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
    private Relationship relationship;
}
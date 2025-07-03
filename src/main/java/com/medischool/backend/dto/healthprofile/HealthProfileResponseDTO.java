package com.medischool.backend.dto.healthprofile;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileResponseDTO {
    
    private UUID healthProfileId;
    private Integer studentId;
    private Integer height;
    private Integer weight;
    private String bloodType;
    private String bloodPressure;
    private Integer rightEye;
    private Integer leftEye;
    private Integer ear;
    private String nose;
    private String throat;
    private String allergies;
    private String chronicConditions;
    private String treatmentHistory;
    private String visionGrade;
    private String hearingGrade;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
} 
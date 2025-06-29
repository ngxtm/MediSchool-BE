package com.medischool.backend.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthProfileResponse {
    UUID profileId;

    String allergies;

    String chronicConditions;


    String treatmentHistory;

    String visionGrade;

    String hearingGrade;

    String bloodType;

    String studentCode;

    String studentName;

    Instant createdAt;

    Instant updatedAt;
}

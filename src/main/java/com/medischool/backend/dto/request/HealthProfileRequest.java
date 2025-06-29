package com.medischool.backend.dto.request;

import com.medischool.backend.model.StudentProfile;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthProfileRequest {
    UUID profileId;

    String allergies;

    String chronicConditions;

    String treatmentHistory;

    String visionGrade;

    String hearingGrade;

    String bloodType;

    Integer studentId;
}

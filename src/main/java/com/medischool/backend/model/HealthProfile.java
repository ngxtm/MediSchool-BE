package com.medischool.backend.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="health_profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthProfile {
    @Id
    @GeneratedValue(generator = "UUID")
    UUID healthProfileId;

    @Column(columnDefinition = "TEXT")
    String allergies;

    @Column(columnDefinition = "TEXT")
    String chronicConditions;

    @Column(columnDefinition = "TEXT")
    String treatmentHistory;

    String visionGrade;

    String hearingGrade;

    String bloodType;

    Instant createdAt;

    Instant updatedAt;

    @OneToOne(mappedBy = "healthProfile")
    StudentProfile studentProfile;


    @PrePersist
    public void healthProfileBeforeCreated() {
        this.createdAt = Instant.now();
    }
    @PreUpdate
    public void healthProfileUpdated() {
        this.updatedAt = Instant.now();
    }

}

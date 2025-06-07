package com.medischool.backend.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;


@Entity
@Table(name="student_profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer studentId;

    @Column(nullable = false,unique = true)
    String studentCode;

    String classCode;

    Integer grade;

    Boolean gender;

    LocalDate dateOfBirth;

    String address;

    LocalDate enrollmentDate;

    String emergencyContact;

    String emergencyPhone;

    String name;

    Integer age;

    Instant createdAt;

    Instant updatedAt;

    @OneToOne
    @JoinColumn(name = "health_profile_id", referencedColumnName = "healthProfileId")
    HealthProfile healthProfile;

    @PrePersist
    public void healthProfileBeforeCreated() {
        this.createdAt = Instant.now();
    }
    @PreUpdate
    public void healthProfileUpdated() {
        this.updatedAt = Instant.now();
    }
}

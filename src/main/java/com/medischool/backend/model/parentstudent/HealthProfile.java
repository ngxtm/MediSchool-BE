package com.medischool.backend.model.parentstudent;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "health_profile")
public class HealthProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "health_profile_id")
    private UUID healthProfileId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "student_id", insertable = false, updatable = false)
    private Integer studentId;
    
    @Column(name = "height")
    private Integer height;
    
    @Column(name = "weight")
    private Integer weight;
    
    @Column(name = "blood_type")
    private String bloodType;
    
    @Column(name = "blood_pressure")
    private String bloodPressure;
    
    @Column(name = "right_eye")
    private Integer rightEye;
    
    @Column(name = "left_eye")
    private Integer leftEye;
    
    @Column(name = "ear")
    private Integer ear;
    
    @Column(name = "nose")
    private String nose;
    
    @Column(name = "throat")
    private String throat;
    
    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;
    
    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;
    
    @Column(name = "treatment_history", columnDefinition = "TEXT")
    private String treatmentHistory;
    
    @Column(name = "vision_grade")
    private String visionGrade;
    
    @Column(name = "hearing_grade")
    private String hearingGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;
}

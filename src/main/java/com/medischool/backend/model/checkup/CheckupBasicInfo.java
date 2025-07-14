package com.medischool.backend.model.checkup;

import com.medischool.backend.model.parentstudent.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkup_basic_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupBasicInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id", unique = true)
    private Student student;

    private String bloodType;
    private String height;
    private String weight;
    private String visionLeft;
    private String visionRight;
    private String underlyingDiseases;
    private String allergies;

    private LocalDateTime updatedAt;
} 
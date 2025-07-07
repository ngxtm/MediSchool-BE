package com.medischool.backend.model.checkup;

import com.medischool.backend.model.parentstudent.Student;
import jakarta.persistence.*;
import lombok.*;
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
    private Double height;
    private Double weight;
    private Double visionLeft;
    private Double visionRight;
    private String underlyingDiseases;
    private String allergies;

    private LocalDateTime updatedAt;
} 
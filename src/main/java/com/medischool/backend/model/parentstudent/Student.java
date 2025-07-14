package com.medischool.backend.model.parentstudent;

import com.medischool.backend.model.enums.Gender;
import com.medischool.backend.model.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "student")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_code")
    private String studentCode;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "class_code")
    private String classCode;


    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String address;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    private String avatar;
}
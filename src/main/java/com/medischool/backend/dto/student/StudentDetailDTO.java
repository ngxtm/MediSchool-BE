package com.medischool.backend.dto.student;

import com.medischool.backend.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetailDTO {
    private String fullName;
    private String studentCode;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String classCode;
    private LocalDate enrollmentDate;

    private String fatherName;
    private String fatherPhone;
    private String motherName;
    private String motherPhone;

    private String emergencyContact;
    private String emergencyPhone;
    private String address;
}

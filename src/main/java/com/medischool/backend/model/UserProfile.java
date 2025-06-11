package com.medischool.backend.model;

import com.medischool.backend.model.enums.Gender;
import com.medischool.backend.model.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String email;

    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}

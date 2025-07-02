package com.medischool.backend.model;


import com.medischool.backend.util.constant.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

import java.time.LocalDate;
import java.util.UUID;

import com.medischool.backend.model.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String email;



    private String address;

    @ManyToOne
    @JoinColumn(name="check_up_result")
    CheckupResult checkupResult;


    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)

    UserRole role;

    private Gender gender;

    private String role;

}

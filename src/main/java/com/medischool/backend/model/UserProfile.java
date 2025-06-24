package com.medischool.backend.model;

import com.medischool.backend.util.constant.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String email;

    private String password;

    private String address;

    @ManyToOne
    @JoinColumn(name="check_up_result")
    CheckupResult checkupResult;


    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    UserRole role;
}

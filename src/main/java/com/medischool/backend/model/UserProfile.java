package com.medischool.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.medischool.backend.model.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
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

    private String role;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private UUID deletedBy;
    
    @Column(name = "delete_reason")
    private String deleteReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

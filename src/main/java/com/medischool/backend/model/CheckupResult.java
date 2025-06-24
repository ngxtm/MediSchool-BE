package com.medischool.backend.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name="checkup_result")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckupResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String result;

    String note;

    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private StudentProfile studentProfile;

    Boolean isNormal;

    Long nurseId;

    @ManyToOne
    @JoinColumn(name = "periodic_id")
    PeriodicCheckup periodicCheckup;








}

package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vaccine")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccine_id")
    private Long vaccineId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(nullable = false)
    private String manufacturer;

    @Column(name = "doses_required")
    private Integer dosesRequired;

    @Column(name = "min_age_months")
    private Integer minAgeMonths;

    @Column(name = "max_age_months")
    private Integer maxAgeMonths;

    @Column(name = "storage_temperature")
    private String storageTemperature;

    @Column(name = "side_effects", length = 1000)
    private String sideEffects;
}
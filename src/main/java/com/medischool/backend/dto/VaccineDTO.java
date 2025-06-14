package com.medischool.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineDTO {
    private Long vaccineId;
    private String name;
    private String description;
    private String manufacturer;
    private Integer dosesRequired;
    private Integer minAgeMonths;
    private Integer maxAgeMonths;
    private String storageTemperature;
    private String sideEffects;
}
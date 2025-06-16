package com.medischool.backend.dto.student;

public record VaccinationDoseDTO(Integer doseNumber,
                                 String doseLabel,
                                 java.time.LocalDate vaccinationDate,
                                 String location,
                                 String vaccineName) {
}

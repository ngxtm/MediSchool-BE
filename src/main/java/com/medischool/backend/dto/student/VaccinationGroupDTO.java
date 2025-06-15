package com.medischool.backend.dto.student;

import java.util.List;

public record VaccinationGroupDTO(String diseaseName,
                                  List<VaccinationDoseDTO> doses) {
}

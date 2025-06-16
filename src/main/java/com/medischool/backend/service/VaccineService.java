package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineDTO;
import com.medischool.backend.dto.student.VaccinationGroupDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VaccineService {
    List<VaccineDTO> getAllVaccines();
    VaccineDTO getVaccineById(int id);
    Page<VaccinationGroupDTO> getHistoryPage(Integer studentId, int page, int size);
}
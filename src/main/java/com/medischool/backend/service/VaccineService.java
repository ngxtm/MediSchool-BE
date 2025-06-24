package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineDTO;

import java.util.List;

public interface VaccineService {
    List<VaccineDTO> getAllVaccines();
    VaccineDTO getVaccineById(int id);
    VaccineDTO createVaccine(VaccineDTO vaccineDTO);
    VaccineDTO updateVaccine(int id, VaccineDTO vaccineDTO);
    boolean deleteVaccine(int id);
}
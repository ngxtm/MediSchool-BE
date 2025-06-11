package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineDTO;
import java.util.List;

public interface VaccineService {
    List<VaccineDTO> getAllVaccines();
}
package com.medischool.backend.service.impl;

import com.medischool.backend.dto.VaccineDTO;
import com.medischool.backend.model.Vaccine;
import com.medischool.backend.repository.VaccineRepository;
import com.medischool.backend.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineServiceImpl implements VaccineService {

    private final VaccineRepository vaccineRepository;

    @Override
    public List<VaccineDTO> getAllVaccines() {
        return vaccineRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VaccineDTO getVaccineById(int id) {
        var vaccineDTO = vaccineRepository.findById(id).orElse(null);
        return convertToDTO(vaccineDTO);
    }

    private VaccineDTO convertToDTO(Vaccine vaccine) {
        VaccineDTO dto = new VaccineDTO();
        dto.setVaccineId(vaccine.getVaccineId());
        dto.setName(vaccine.getName());
        dto.setDescription(vaccine.getDescription());
        dto.setManufacturer(vaccine.getManufacturer());
        dto.setDosesRequired(vaccine.getDosesRequired());
        dto.setSideEffects(vaccine.getSideEffects());
        dto.setMaxAgeMonths(vaccine.getMaxAgeMonths());
        dto.setMinAgeMonths(vaccine.getMinAgeMonths());
        dto.setStorageTemperature(vaccine.getStorageTemperature());

        return dto;
    }
}
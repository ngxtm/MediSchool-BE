package com.medischool.backend.service.impl;

import com.medischool.backend.dto.VaccineDTO;
import com.medischool.backend.model.vaccine.Vaccine;
import com.medischool.backend.projection.VaccinationRow;
import com.medischool.backend.repository.VaccinationHistoryRepository;
import com.medischool.backend.repository.VaccineRepository;
import com.medischool.backend.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class VaccineServiceImpl implements VaccineService {

    private final VaccineRepository vaccineRepository;
    private final VaccinationHistoryRepository repo;

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
        if (vaccineDTO != null) return convertToDTO(vaccineDTO);
        return null;
    }

    private VaccineDTO convertToDTO(Vaccine vaccine) {
        VaccineDTO dto = new VaccineDTO();
        dto.setVaccineId(vaccine.getVaccineId());
        dto.setName(vaccine.getName());
        dto.setDescription(vaccine.getDescription());
        dto.setManufacturer(vaccine.getManufacturer());
        dto.setDosesRequired(vaccine.getDosesRequired());
        dto.setSideEffects(vaccine.getSideEffects());
        dto.setStorageTemperature(vaccine.getStorageTemperature());
        dto.setCategoryId(vaccine.getCategoryId());
        return dto;
    }

    @Override
    public VaccineDTO createVaccine(VaccineDTO vaccineDTO) {
        Vaccine vaccine = new Vaccine();
        vaccine.setName(vaccineDTO.getName());
        vaccine.setDescription(vaccineDTO.getDescription());
        vaccine.setManufacturer(vaccineDTO.getManufacturer());
        vaccine.setDosesRequired(vaccineDTO.getDosesRequired());
        vaccine.setSideEffects(vaccineDTO.getSideEffects());
        vaccine.setStorageTemperature(vaccineDTO.getStorageTemperature());
        vaccine.setCategoryId(vaccineDTO.getCategoryId());

        Vaccine saved = vaccineRepository.save(vaccine);
        return convertToDTO(saved);
    }

    @Override
    public boolean deleteVaccine(int id) {
        if (vaccineRepository.existsById(id)) {
            vaccineRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public VaccineDTO updateVaccine(int id, VaccineDTO vaccineDTO) {
        Optional<Vaccine> optionalVaccine = vaccineRepository.findById(id);
        if (optionalVaccine.isPresent()) {
            Vaccine vaccine = optionalVaccine.get();

            vaccine.setName(vaccineDTO.getName());
            vaccine.setDescription(vaccineDTO.getDescription());
            vaccine.setManufacturer(vaccineDTO.getManufacturer());
            vaccine.setDosesRequired(vaccineDTO.getDosesRequired());
            vaccine.setSideEffects(vaccineDTO.getSideEffects());
            vaccine.setStorageTemperature(vaccineDTO.getStorageTemperature());
            vaccine.setCategoryId(vaccineDTO.getCategoryId());

            Vaccine updated = vaccineRepository.save(vaccine);
            return convertToDTO(updated);
        }
        return null;
    }
}
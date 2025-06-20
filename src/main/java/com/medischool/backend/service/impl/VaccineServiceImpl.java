package com.medischool.backend.service.impl;

import com.medischool.backend.dto.VaccineDTO;
import com.medischool.backend.dto.student.VaccinationDoseDTO;
import com.medischool.backend.dto.student.VaccinationGroupDTO;
import com.medischool.backend.model.Vaccine.Vaccine;
import com.medischool.backend.projection.VaccinationRow;
import com.medischool.backend.repository.VaccinationHistoryRepository;
import com.medischool.backend.repository.VaccineRepository;
import com.medischool.backend.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public Page<VaccinationGroupDTO> getHistoryPage(
            Integer studentId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("categoryName").ascending());

        Page<VaccinationRow> raw = repo.findRowsByStudentId(studentId, pageable);

        // group rows => List<VaccinationGroupDTO>
        List<VaccinationGroupDTO> groups = groupRows(raw.getContent());

        return new PageImpl<>(groups, pageable, raw.getTotalElements()); // PageImpl là cách gói DTO tuỳ ý :contentReference[oaicite:2]{index=2}
    }

    private List<VaccinationGroupDTO> groupRows(List<VaccinationRow> rows) {

        Map<String, List<VaccinationDoseDTO>> grouped =
                rows.stream().collect(Collectors.groupingBy(
                        VaccinationRow::getCategoryName,
                        LinkedHashMap::new,
                        Collectors.mapping(this::toDose, Collectors.toList())
                ));

        return grouped.entrySet()
                .stream()
                .map(e -> new VaccinationGroupDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private VaccinationDoseDTO toDose(VaccinationRow r) {
        return new VaccinationDoseDTO(
                r.getDoseNumber(),
                "Mũi " + r.getDoseNumber(),
                r.getVaccinationDate(),
                r.getLocation(),
                r.getVaccineName());
    }
}
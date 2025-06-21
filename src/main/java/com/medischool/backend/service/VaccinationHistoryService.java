package com.medischool.backend.service;

import com.medischool.backend.dto.VaccinationHistoryRequestDTO;
import com.medischool.backend.model.Vaccine.VaccinationHistory;
import com.medischool.backend.repository.VaccinationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VaccinationHistoryService {
    private final VaccinationHistoryRepository vaccinationHistoryRepository;

    public VaccinationHistory save(VaccinationHistoryRequestDTO dto) {
        VaccinationHistory history = new VaccinationHistory();
        history.setStudentId(dto.getStudentId());
        history.setEventId(dto.getEventId());
        history.setVaccineName(dto.getVaccineName());
        history.setDoseNumber(dto.getDoseNumber());
        history.setVaccinationDate(dto.getVaccinationDate());
        history.setLocation(dto.getLocation());
        history.setNote(dto.getNote());
        history.setCreatedBy(dto.getCreatedBy());
        history.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.LocalDateTime.now());
        return vaccinationHistoryRepository.save(history);
    }
}
package com.medischool.backend.service;

import com.medischool.backend.dto.VaccinationHistoryRequestDTO;
import com.medischool.backend.dto.VaccinationHistoryUpdateDTO;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.repository.VaccinationHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationHistoryService {
    private final VaccinationHistoryRepository vaccinationHistoryRepository;

    public VaccinationHistory save(VaccinationHistoryRequestDTO dto) {
        VaccinationHistory history = new VaccinationHistory();
        history.setStudentId(dto.getStudentId());
        history.setEventId(dto.getEventId());
        history.setVaccine(dto.getVaccine());
        history.setDoseNumber(dto.getDoseNumber());
        history.setVaccinationDate(dto.getVaccinationDate());
        history.setLocation(dto.getLocation());
        history.setNote(dto.getNote());
        history.setCreatedBy(dto.getCreatedBy());
        history.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.LocalDateTime.now());
        return vaccinationHistoryRepository.save(history);
    }

    public List<VaccinationHistory> findByEventId(Long eventId) {
        return vaccinationHistoryRepository.findByEventId(eventId);
    }
}
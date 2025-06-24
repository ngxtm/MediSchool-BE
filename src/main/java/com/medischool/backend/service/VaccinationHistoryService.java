package com.medischool.backend.service;

import com.medischool.backend.dto.VaccinationHistoryRequestDTO;
import com.medischool.backend.dto.VaccinationHistoryUpdateDTO;
import com.medischool.backend.dto.VaccinationHistoryWithStudentDTO;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.model.vaccine.VaccineCategory;
import com.medischool.backend.model.vaccine.Vaccine;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.VaccinationHistoryRepository;
import com.medischool.backend.repository.VaccineCategoryRepository;
import com.medischool.backend.repository.VaccineRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationHistoryService {
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final VaccineCategoryRepository vaccineCategoryRepository;
    private final VaccineRepository vaccineRepository;
    private final StudentRepository studentRepository;

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

    public List<VaccinationHistoryWithStudentDTO> findByEventIdWithStudent(Long eventId) {
        List<VaccinationHistory> histories = vaccinationHistoryRepository.findByEventId(eventId);
        List<VaccinationHistoryWithStudentDTO> result = new ArrayList<>();
        for (VaccinationHistory history : histories) {
            Student student = studentRepository.findById(history.getStudentId()).orElse(null);
            VaccinationHistoryWithStudentDTO dto = new VaccinationHistoryWithStudentDTO();
            dto.setHistory(history);
            dto.setStudent(student);
            result.add(dto);
        }
        return result;
    }

    public Map<String, List<VaccinationHistory>> getStudentHistoryGroupedByCategory(Integer studentId) {
        List<VaccinationHistory> allHistories = vaccinationHistoryRepository.findAll()
            .stream().filter(h -> h.getStudentId().equals(studentId)).collect(Collectors.toList());
        Map<Integer, List<VaccinationHistory>> byCategory = allHistories.stream()
            .collect(Collectors.groupingBy(h -> h.getVaccine().getCategoryId()));
        Map<Integer, String> categoryNames = vaccineCategoryRepository.findAll().stream()
            .collect(Collectors.toMap(c -> c.getCategoryId(), c -> c.getCategoryName()));
        Map<String, List<VaccinationHistory>> result = new HashMap<>();
        for (Map.Entry<Integer, List<VaccinationHistory>> entry : byCategory.entrySet()) {
            String catName = categoryNames.getOrDefault(entry.getKey(), "Unknown");
            result.put(catName, entry.getValue());
        }
        return result;
    }
}
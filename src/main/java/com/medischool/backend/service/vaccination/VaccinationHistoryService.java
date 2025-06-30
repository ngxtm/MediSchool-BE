package com.medischool.backend.service.vaccination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.vaccination.VaccinationHistoryBulkUpdateDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryBulkUpdateResponseDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryRequestDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryUpdateDTO;
import com.medischool.backend.dto.vaccination.VaccinationHistoryWithStudentDTO;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.vaccination.VaccinationHistoryRepository;
import com.medischool.backend.repository.vaccination.VaccineCategoryRepository;
import com.medischool.backend.repository.vaccination.VaccineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public Optional<VaccinationHistory> update(Integer historyId, VaccinationHistoryUpdateDTO dto) {
        Optional<VaccinationHistory> historyOptional = vaccinationHistoryRepository.findById(historyId);
        
        if (historyOptional.isEmpty()) {
            return Optional.empty();
        }
        
        VaccinationHistory history = historyOptional.get();
        
        if (dto.getDoseNumber() != null) {
            history.setDoseNumber(dto.getDoseNumber());
        }
        
        if (dto.getVaccinationDate() != null) {
            history.setVaccinationDate(dto.getVaccinationDate());
        }
        
        if (dto.getLocation() != null) {
            history.setLocation(dto.getLocation());
        }
        
        if (dto.getNote() != null) {
            history.setNote(dto.getNote());
        }
        
        if (dto.getAbnormal() != null) {
            history.setAbnormal(dto.getAbnormal());
        }
        
        if (dto.getFollowUpNote() != null) {
            history.setFollowUpNote(dto.getFollowUpNote());
        }
        
        VaccinationHistory updatedHistory = vaccinationHistoryRepository.save(history);
        return Optional.of(updatedHistory);
    }

    @Transactional
    public VaccinationHistoryBulkUpdateResponseDTO bulkUpdate(VaccinationHistoryBulkUpdateDTO bulkUpdateDTO) {
        List<VaccinationHistory> updatedHistories = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (VaccinationHistoryBulkUpdateDTO.VaccinationHistoryUpdateItem item : bulkUpdateDTO.getUpdates()) {
            try {
                Optional<VaccinationHistory> historyOptional = vaccinationHistoryRepository.findById(item.getHistoryId());
                
                if (historyOptional.isPresent()) {
                    VaccinationHistory history = historyOptional.get();
                    
                    if (item.getDoseNumber() != null) {
                        history.setDoseNumber(item.getDoseNumber());
                    }
                    
                    if (item.getVaccinationDate() != null) {
                        history.setVaccinationDate(item.getVaccinationDate());
                    }
                    
                    if (item.getLocation() != null) {
                        history.setLocation(item.getLocation());
                    }
                    
                    if (item.getNote() != null) {
                        history.setNote(item.getNote());
                    }
                    
                    if (item.getAbnormal() != null) {
                        history.setAbnormal(item.getAbnormal());
                    }
                    
                    if (item.getFollowUpNote() != null) {
                        history.setFollowUpNote(item.getFollowUpNote());
                    }
                    
                    VaccinationHistory savedHistory = vaccinationHistoryRepository.save(history);
                    updatedHistories.add(savedHistory);
                    
                    log.info("Updated vaccination history with ID: {}", item.getHistoryId());
                } else {
                    String errorMsg = "Vaccination history with ID " + item.getHistoryId() + " not found";
                    errors.add(errorMsg);
                    log.warn(errorMsg);
                }
            } catch (Exception e) {
                String errorMsg = "Failed to update vaccination history with ID " + item.getHistoryId() + ": " + e.getMessage();
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }
        
        return new VaccinationHistoryBulkUpdateResponseDTO(
            bulkUpdateDTO.getUpdates().size(),
            updatedHistories,
            errors
        );
    }

    public Optional<VaccinationHistory> findById(Integer historyId) {
        return vaccinationHistoryRepository.findById(historyId);
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
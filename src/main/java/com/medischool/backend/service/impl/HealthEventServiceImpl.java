package com.medischool.backend.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.healthevent.request.HealthEventRequestDTO;
import com.medischool.backend.dto.healthevent.response.HealthEventResponseDTO;
import com.medischool.backend.dto.healthevent.response.TotalHealthEventStatusResDTO;
import com.medischool.backend.model.healthevent.EventMedicine;
import com.medischool.backend.model.healthevent.HealthEvent;
import com.medischool.backend.repository.healthevent.EventMedicineRepository;
import com.medischool.backend.repository.healthevent.HealthEventRepository;
import com.medischool.backend.service.healthevent.HealthEventService;
import com.medischool.backend.service.healthevent.MedicineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthEventServiceImpl implements HealthEventService {

    private final HealthEventRepository healthEventRepository;
    private final EventMedicineRepository eventMedicineRepository;
    private final MedicineService medicineService;

    public TotalHealthEventStatusResDTO getTotalHealthEventStatusResDTO() {
        int totalEvent = healthEventRepository.findAllWithStudent().size();
        int normalEvent = healthEventRepository.findByExtentWithStudent("NORMAL").size();
        int dangerousEvent = healthEventRepository.findByExtentWithStudent("DANGEROUS").size();
        
        return TotalHealthEventStatusResDTO.builder()
                .totalHealthEvent(totalEvent)
                .totalNormalCase(normalEvent)
                .totalDangerousCase(dangerousEvent)
                .build();
    }

    @Override
    public List<HealthEventResponseDTO> getAllHealthEvent() {
        List<HealthEvent> healthEvents = healthEventRepository.findAllWithStudent();
        return healthEvents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HealthEventResponseDTO convertToDTO(HealthEvent healthEvent) {
        return HealthEventResponseDTO.builder()
                .id(healthEvent.getId())
                .studentId(healthEvent.getStudentId())
                .student(healthEvent.getStudent())
                .problem(healthEvent.getProblem())
                .description(healthEvent.getDescription())
                .solution(healthEvent.getSolution())
                .location(healthEvent.getLocation())
                .eventTime(healthEvent.getEventTime())
                .recordBy(healthEvent.getRecordBy())
                .extent(healthEvent.getExtent())
                .eventMedicines(healthEvent.getEventMedicines())
                .build();
    }

    @Override
    @Transactional
    public HealthEvent createHealthEvent(HealthEventRequestDTO requestDTO) {
        HealthEvent healthEvent = new HealthEvent();
        healthEvent.setStudentId(requestDTO.getStudentId());
        healthEvent.setProblem(requestDTO.getProblem());
        healthEvent.setDescription(requestDTO.getDescription());
        healthEvent.setSolution(requestDTO.getSolution());
        healthEvent.setLocation(requestDTO.getLocation());
        healthEvent.setEventTime(requestDTO.getEventTime() != null ? requestDTO.getEventTime() : OffsetDateTime.now());
        healthEvent.setRecordBy(requestDTO.getRecordBy());
        healthEvent.setExtent(requestDTO.getExtent());
        
        HealthEvent savedEvent = healthEventRepository.save(healthEvent);

        if (requestDTO.getMedicines() != null && !requestDTO.getMedicines().isEmpty()) {
            for (HealthEventRequestDTO.EventMedicineDTO medicineDTO : requestDTO.getMedicines()) {
                boolean updateSuccess = medicineService.updateMedicineQuantity(
                    medicineDTO.getMedicineId(),
                    medicineDTO.getQuantity()
                );
                
                if (!updateSuccess) {
                    throw new RuntimeException("Không đủ số lượng thuốc với ID: " + medicineDTO.getMedicineId() + 
                                             " hoặc thuốc không tồn tại");
                }
                
                // Tạo record trong event_medicine
                EventMedicine eventMedicine = new EventMedicine();
                eventMedicine.setEventId(savedEvent.getId());
                eventMedicine.setMedicineId(medicineDTO.getMedicineId());
                eventMedicine.setQuantity(medicineDTO.getQuantity());
                eventMedicine.setUnit(medicineDTO.getUnit());
                eventMedicine.setNote(medicineDTO.getNote());
                eventMedicineRepository.save(eventMedicine);
            }
        }
        
        return savedEvent;
    }

    @Override
    public HealthEvent getHealthEventById(Long id) {
        return healthEventRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public HealthEvent updateHealthEvent(Long id, HealthEventRequestDTO requestDTO) {
        HealthEvent existingEvent = healthEventRepository.findById(id).orElse(null);
        if (existingEvent == null) {
            return null;
        }

        if (requestDTO.getStudentId() != null) {
            existingEvent.setStudentId(requestDTO.getStudentId());
        }
        if (requestDTO.getProblem() != null) {
            existingEvent.setProblem(requestDTO.getProblem());
        }
        if (requestDTO.getDescription() != null) {
            existingEvent.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getSolution() != null) {
            existingEvent.setSolution(requestDTO.getSolution());
        }
        if (requestDTO.getLocation() != null) {
            existingEvent.setLocation(requestDTO.getLocation());
        }
        if (requestDTO.getEventTime() != null) {
            existingEvent.setEventTime(requestDTO.getEventTime());
        }
        if (requestDTO.getExtent() != null) {
            existingEvent.setExtent(requestDTO.getExtent());
        }
        
        HealthEvent updatedEvent = healthEventRepository.save(existingEvent);

        if (requestDTO.getMedicines() != null) {
            eventMedicineRepository.deleteByEventId(id);

            for (HealthEventRequestDTO.EventMedicineDTO medicineDTO : requestDTO.getMedicines()) {
                boolean updateSuccess = medicineService.updateMedicineQuantity(
                    medicineDTO.getMedicineId(),
                    medicineDTO.getQuantity()
                );
                
                if (!updateSuccess) {
                    throw new RuntimeException("Không đủ số lượng thuốc với ID: " + medicineDTO.getMedicineId() + 
                                             " hoặc thuốc không tồn tại");
                }
                
                EventMedicine eventMedicine = new EventMedicine();
                eventMedicine.setEventId(updatedEvent.getId());
                eventMedicine.setMedicineId(medicineDTO.getMedicineId());
                eventMedicine.setQuantity(medicineDTO.getQuantity());
                eventMedicine.setUnit(medicineDTO.getUnit());
                eventMedicine.setNote(medicineDTO.getNote());
                eventMedicineRepository.save(eventMedicine);
            }
        }
        
        return updatedEvent;
    }

    @Override
    @Transactional
    public void deleteHealthEvent(Long id) {
        eventMedicineRepository.deleteByEventId(id);
        healthEventRepository.deleteById(id);
    }
}

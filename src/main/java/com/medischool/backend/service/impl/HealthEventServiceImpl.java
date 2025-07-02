package com.medischool.backend.service.impl;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.healthevent.request.HealthEventRequestDTO;
import com.medischool.backend.dto.healthevent.response.TotalHealthEventStatusResDTO;
import com.medischool.backend.model.healthevent.EventMedicine;
import com.medischool.backend.model.healthevent.HealthEvent;
import com.medischool.backend.repository.healthevent.EventMedicineRepository;
import com.medischool.backend.repository.healthevent.HealthEventRepository;
import com.medischool.backend.service.healthevent.HealthEventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthEventServiceImpl implements HealthEventService {

    private final HealthEventRepository healthEventRepository;
    private final EventMedicineRepository eventMedicineRepository;

    public TotalHealthEventStatusResDTO getTotalHealthEventStatusResDTO() {
        int totalEvent = healthEventRepository.findAll().size();
        int normalEvent = healthEventRepository.findByExtent("NORMAL").size();
        int dangerousEvent = healthEventRepository.findByExtent("DANGEROUS").size();
        
        return TotalHealthEventStatusResDTO.builder()
                .totalHealthEvent(totalEvent)
                .totalNormalCase(normalEvent)
                .totalDangerousCase(dangerousEvent)
                .build();
    }

    @Override
    public List<HealthEvent> getAllHealthEvent() {
        return healthEventRepository.findAll();
    }

    @Override
    @Transactional
    public HealthEvent createHealthEvent(HealthEventRequestDTO requestDTO) {
        HealthEvent healthEvent = new HealthEvent();
        healthEvent.setStudentId(requestDTO.getStudentId());
        healthEvent.setProblem(requestDTO.getProblem());
        healthEvent.setDescription(requestDTO.getDescription());
        healthEvent.setSolution(requestDTO.getSolution());
        healthEvent.setEventTime(requestDTO.getEventTime() != null ? requestDTO.getEventTime() : OffsetDateTime.now());
        healthEvent.setRecordBy(requestDTO.getRecordBy());
        healthEvent.setExtent(requestDTO.getExtent());
        
        HealthEvent savedEvent = healthEventRepository.save(healthEvent);
        
        if (requestDTO.getMedicines() != null && !requestDTO.getMedicines().isEmpty()) {
            for (HealthEventRequestDTO.EventMedicineDTO medicineDTO : requestDTO.getMedicines()) {
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

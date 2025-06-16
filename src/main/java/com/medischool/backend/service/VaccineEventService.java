package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.model.Vaccine;
import com.medischool.backend.model.VaccineEvent;
import com.medischool.backend.repository.VaccineEventRepository;
import com.medischool.backend.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VaccineEventService {
    private final VaccineEventRepository vaccineEventRepository;
    private final VaccineRepository vaccineRepository;

    public VaccineEvent createVaccineEvent(VaccineEventRequestDTO requestDTO) {
        Vaccine vaccine = vaccineRepository.findById(Math.toIntExact(requestDTO.getVaccineId()))
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));

        VaccineEvent event = new VaccineEvent();
        event.setVaccine(vaccine);
        event.setEventTitle(requestDTO.getEventTitle());
        event.setEventDate(requestDTO.getEventDate());
        event.setEventScope(requestDTO.getEventScope());
        event.setLocation(requestDTO.getLocation());
        event.setStatus(requestDTO.getStatus());
        event.setCreatedAt(LocalDateTime.now());

        return vaccineEventRepository.save(event);
    }

}
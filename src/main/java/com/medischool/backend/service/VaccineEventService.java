package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.model.VaccinationConsent;
import com.medischool.backend.model.Vaccine;
import com.medischool.backend.model.VaccineEvent;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.repository.ConsentRepository;
import com.medischool.backend.repository.VaccineEventRepository;
import com.medischool.backend.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineEventService {
    private final VaccineEventRepository vaccineEventRepository;
    private final VaccineRepository vaccineRepository;
    private final ConsentRepository consentRepository;
    private final JdbcTemplate jdbcTemplate;

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

    public List<VaccineEvent> getAllVaccineEvents() {
        return vaccineEventRepository.findAll();
    }


    public Map<String, Object> sendConsentsToUnvaccinatedStudents(Long eventId) {
        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event with ID " + eventId + " not found"));

        String insertSql = """
                    INSERT INTO vaccination_consent (
                        student_id,
                        event_id,
                        parent_id,
                        consent_status,
                        created_at,
                        ready_to_sent
                    )
                    SELECT 
                        s.student_id,
                        ?,
                        psl.parent_id,
                        'APPROVE',
                        NOW(),
                        true
                    FROM student s
                    JOIN parent_student_link psl ON s.student_id = psl.student_id
                    WHERE NOT EXISTS (
                        SELECT 1 FROM vaccination_history vh 
                        WHERE vh.student_id = s.student_id
                    )
                    AND NOT EXISTS (
                        SELECT 1 FROM vaccination_consent c 
                        WHERE c.student_id = s.student_id AND c.event_id = ?
                    )
                """;

        // Truyền eventId 2 lần vì dùng cho SELECT và WHERE
        int insertedCount = jdbcTemplate.update(insertSql, eventId, eventId);

        return Map.of(
                "success", true,
                "message", String.format("Successfully created %d consents", insertedCount),
                "event_id", eventId,
                "consents_sent", insertedCount
        );
    }
}
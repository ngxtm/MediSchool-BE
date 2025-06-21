package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.Vaccine.VaccinationHistory;
import com.medischool.backend.model.Vaccine.Vaccine;
import com.medischool.backend.model.Vaccine.VaccineEvent;
import com.medischool.backend.model.enums.EventStatus;
import com.medischool.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.medischool.backend.model.Vaccine.VaccineEventClass;

@Service
@RequiredArgsConstructor
public class VaccineEventService {
    private final VaccineEventRepository vaccineEventRepository;
    private final VaccineRepository vaccineRepository;
    private final ConsentRepository consentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final VaccineEventClassRepository vaccineEventClassRepository;
    private final VaccinationHistoryRepository vaccinationHistoryRepository;
    private final UserProfileRepository userProfileRepository;

    public VaccineEvent createVaccineEvent(VaccineEventRequestDTO requestDTO) {
        Vaccine vaccine = vaccineRepository.findById(Math.toIntExact(requestDTO.getVaccineId()))
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        VaccineEvent event = new VaccineEvent();

        UserProfile creator = userProfileRepository.findById(requestDTO.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        event.setVaccine(vaccine);
        event.setEventTitle(requestDTO.getEventTitle());
        event.setEventDate(requestDTO.getEventDate());
        event.setEventScope(requestDTO.getEventScope());
        event.setLocation(requestDTO.getLocation());
        event.setStatus(requestDTO.getStatus());
        event.setCreatedAt(LocalDateTime.now());
        event.setCreatedBy(creator);


        event = vaccineEventRepository.save(event);

        for (String classCode : requestDTO.getClasses()) {
            if (!vaccineEventClassRepository.existsByEventIdAndClassCode(event.getId(), classCode)) {
                VaccineEventClass link = new VaccineEventClass();
                link.setEventId(event.getId());
                link.setClassCode(classCode);
                vaccineEventClassRepository.save(link);
            }
        }
        return event;
    }

    public List<VaccineEvent> getAllVaccineEvents() {
        return vaccineEventRepository.findAll();
    }

    public VaccineEvent updateEventStatus(Long eventId, String status) {
        EventStatus newStatus;
        try {
            newStatus = EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be one of: PENDING, APPROVED, COMPLETED, CANCELLED");
        }

        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event not found"));

        event.setStatus(newStatus);
        return vaccineEventRepository.save(event);
    }

    public Map<String, Object> sendConsentsToUnvaccinatedStudents(Long eventId) {
        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event with ID " + eventId + " not found"));

        int insertedCount = 0;

        String sql;
        if ("SCHOOL".equalsIgnoreCase(event.getEventScope().toString())) {
            sql = """
                        INSERT INTO vaccination_consent (
                            student_id, event_id, parent_id, consent_status, created_at
                        )
                        SELECT s.student_id, ?, psl.parent_id, NULL, NOW()
                        FROM student s
                        JOIN parent_student_link psl ON s.student_id = psl.student_id
                        WHERE NOT EXISTS (
                            SELECT 1 FROM vaccination_history vh 
                            WHERE vh.student_id = s.student_id AND vh.event_id = ?
                        )
                        AND NOT EXISTS (
                            SELECT 1 FROM vaccination_consent c 
                            WHERE c.student_id = s.student_id AND c.event_id = ?
                        )
                    """;
            insertedCount = jdbcTemplate.update(sql, eventId, eventId, eventId);

        } else if ("CLASS".equalsIgnoreCase(event.getEventScope().toString())) {
            sql = """
                        INSERT INTO vaccination_consent (
                            student_id, event_id, parent_id, consent_status, created_at
                        )
                        SELECT s.student_id, ?, psl.parent_id, NULL, NOW()
                        FROM student s
                        JOIN parent_student_link psl ON s.student_id = psl.student_id
                        JOIN vaccine_event_class vec ON TRIM(vec.class_code) = TRIM(s.class_code)
                        WHERE vec.event_id = ?
                          AND NOT EXISTS (
                              SELECT 1 FROM vaccination_history vh 
                              WHERE vh.student_id = s.student_id AND vh.event_id = ?
                          )
                          AND NOT EXISTS (
                              SELECT 1 FROM vaccination_consent c 
                              WHERE c.student_id = s.student_id AND c.event_id = ?
                          )
                    """;
            insertedCount = jdbcTemplate.update(sql, eventId, eventId, eventId, eventId);
        }

        return Map.of(
                "success", true,
                "event_id", eventId,
                "message", "Consents created successfully",
                "consents_sent", insertedCount
        );
    }


    public List<VaccineEvent> getVaccineEventsByYear(int year) {
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("Invalid year");
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return vaccineEventRepository.findAllByEventDateBetween(startDate, endDate);
    }

    public Optional<VaccineEvent> getVaccineEventById(Long eventId) {
        return vaccineEventRepository.findById(eventId);
    }

}
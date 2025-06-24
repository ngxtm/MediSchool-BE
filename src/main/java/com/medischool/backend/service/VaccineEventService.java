package com.medischool.backend.service;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.*;
import com.medischool.backend.model.enums.EventStatus;
import com.medischool.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    private List<Integer> getAllStudentIdsInSchool() {
        return studentRepository.findAll()
                .stream()
                .map(student -> student.getStudentId())
                .collect(Collectors.toList());
    }

    private List<Integer> getAllStudentIdsInClasses(List<String> classCodes) {
        return studentRepository.findByClassCodeIn(classCodes)
                .stream()
                .map(student -> student.getStudentId())
                .collect(Collectors.toList());
    }

    private UUID getParentIdForStudent(Integer studentId) {
        return parentStudentLinkRepository.findByStudentId(studentId)
                .stream()
                .findFirst()
                .map(link -> link.getParentId())
                .orElse(null);
    }

    public VaccineEvent createVaccineEvent(VaccineEventRequestDTO requestDTO) {
        Vaccine vaccine = vaccineRepository.findById(Math.toIntExact(requestDTO.getVaccineId()))
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        VaccineEvent event = new VaccineEvent();

//        UserProfile creator = userProfileRepository.findById(requestDTO.getCreatedBy())
//                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = auth.getName();
        UUID userId = UUID.fromString(userIdStr);

        UserProfile profileOpt = userProfileRepository.findSingleById(userId);

        event.setVaccine(vaccine);
        event.setEventTitle(requestDTO.getEventTitle());
        event.setEventDate(requestDTO.getEventDate());
        event.setEventScope(requestDTO.getEventScope());
        event.setLocation(requestDTO.getLocation());
        event.setStatus(requestDTO.getStatus());
        event.setCreatedAt(LocalDateTime.now());
        event.setCreatedBy(profileOpt);


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
            throw new IllegalArgumentException("Status must be one of: APPROVE, REJECT");
        }

        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event not found"));

        event.setStatus(newStatus);
        return vaccineEventRepository.save(event);
    }

    public Map<String, Object> sendConsentsToUnvaccinatedStudents(Long eventId) {
        VaccineEvent event = vaccineEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Vaccine event with ID " + eventId + " not found"));

        Vaccine vaccine = event.getVaccine();
        Integer categoryId = vaccine.getCategoryId();
        Integer dosesRequired = vaccine.getDosesRequired();

        List<Integer> studentIds = new ArrayList<>();
        if ("SCHOOL".equalsIgnoreCase(event.getEventScope().toString())) {
            studentIds = getAllStudentIdsInSchool();
        } else if ("CLASS".equalsIgnoreCase(event.getEventScope().toString())) {
            List<VaccineEventClass> eventClasses = vaccineEventClassRepository.findAllByEventId(eventId);
            List<String> classCodes = eventClasses.stream().map(VaccineEventClass::getClassCode).toList();
            studentIds = getAllStudentIdsInClasses(classCodes);
        }

        int insertedCount = 0;
        for (Integer studentId : studentIds) {
            boolean consentExists = consentRepository.existsByStudentIdAndEventId(studentId, eventId);
            if (consentExists) continue;

            List<VaccinationHistory> histories = vaccinationHistoryRepository.findByStudentIdAndVaccine_CategoryId(studentId, categoryId);
            if (histories.size() >= dosesRequired) continue;

            UUID parentId = getParentIdForStudent(studentId);
            if (parentId == null) {
                continue;
            }

            VaccinationConsent consent = new VaccinationConsent();
            consent.setStudentId(studentId);
            consent.setEventId(eventId);
            consent.setParentId(parentId);
            consent.setConsentStatus(null);
            consent.setCreatedAt(LocalDateTime.now());
            consentRepository.save(consent);
            insertedCount++;
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

    public List<VaccineEvent> getUpcomingVaccineEvent() {
        return vaccineEventRepository.findAllByEventDateAfter(LocalDate.now());
    }


    public int createVaccinationHistoryForAgreedConsents(Long eventId) {
        List<VaccinationConsent> agreedConsents = consentRepository.findAllByEventIdAndConsentStatus(eventId, ConsentStatus.APPROVE);
        Optional<VaccineEvent> eventOpt = vaccineEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) return 0;
        VaccineEvent event = eventOpt.get();
        Vaccine vaccine = event.getVaccine();

        int count = 0;
        for (VaccinationConsent consent : agreedConsents) {
            boolean exists = vaccinationHistoryRepository.existsByStudentIdAndEventId(consent.getStudentId(), eventId);
            if (!exists) {
                Integer categoryId = vaccine.getCategoryId();
                List<VaccinationHistory> histories = vaccinationHistoryRepository.findByStudentIdAndVaccine_CategoryId(consent.getStudentId(), categoryId);
                int doseNumber = histories.size() + 1;

                VaccinationHistory history = new VaccinationHistory();
                history.setStudentId(consent.getStudentId());
                history.setEventId(eventId);
                history.setVaccine(vaccine);
                history.setDoseNumber(doseNumber);

                history.setVaccinationDate(event.getEventDate());
                history.setLocation(event.getLocation());
                history.setNote(consent.getNote());
                history.setAbnormal(false);
                history.setFollowUpNote(null);

                history.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                history.setCreatedAt(LocalDateTime.now());

                vaccinationHistoryRepository.save(history);
                count++;
            }
        }
        return count;
    }
}
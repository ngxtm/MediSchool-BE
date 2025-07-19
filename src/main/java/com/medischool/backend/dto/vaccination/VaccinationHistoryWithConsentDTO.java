package com.medischool.backend.dto.vaccination;

import com.medischool.backend.model.vaccine.Vaccine;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class VaccinationHistoryWithConsentDTO {
    private Integer historyId;
    private Integer studentId;
    private Long eventId;
    private Vaccine vaccine;
    private Integer doseNumber;
    private LocalDate vaccinationDate;
    private String location;
    private String note;
    private Boolean abnormal;
    private String followUpNote;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private Long consentId;

    // getters, setters, constructor
    public VaccinationHistoryWithConsentDTO() {}

    public VaccinationHistoryWithConsentDTO(Integer historyId, Integer studentId, Long eventId, Vaccine vaccine, Integer doseNumber, LocalDate vaccinationDate, String location, String note, Boolean abnormal, String followUpNote, UUID createdBy, LocalDateTime createdAt, Long consentId) {
        this.historyId = historyId;
        this.studentId = studentId;
        this.eventId = eventId;
        this.vaccine = vaccine;
        this.doseNumber = doseNumber;
        this.vaccinationDate = vaccinationDate;
        this.location = location;
        this.note = note;
        this.abnormal = abnormal;
        this.followUpNote = followUpNote;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.consentId = consentId;
    }

    // ... getters and setters ...
    public Integer getHistoryId() { return historyId; }
    public void setHistoryId(Integer historyId) { this.historyId = historyId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Vaccine getVaccine() { return vaccine; }
    public void setVaccine(Vaccine vaccine) { this.vaccine = vaccine; }
    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer doseNumber) { this.doseNumber = doseNumber; }
    public LocalDate getVaccinationDate() { return vaccinationDate; }
    public void setVaccinationDate(LocalDate vaccinationDate) { this.vaccinationDate = vaccinationDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Boolean getAbnormal() { return abnormal; }
    public void setAbnormal(Boolean abnormal) { this.abnormal = abnormal; }
    public String getFollowUpNote() { return followUpNote; }
    public void setFollowUpNote(String followUpNote) { this.followUpNote = followUpNote; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getConsentId() { return consentId; }
    public void setConsentId(Long consentId) { this.consentId = consentId; }
} 
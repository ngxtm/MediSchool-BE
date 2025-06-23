package com.medischool.backend.model.vaccine;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccination_history")
@Data
public class VaccinationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    @Column(name = "dose_number")
    private Integer doseNumber;

    @Column(name = "vaccination_date")
    private LocalDate vaccinationDate;

    @Column(name = "location")
    private String location;

    @Column(name = "note")
    private String note;

    @Column(name = "abnormal")
    private Boolean abnormal = false;

    @Column(name = "follow_up_note")
    private String followUpNote;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
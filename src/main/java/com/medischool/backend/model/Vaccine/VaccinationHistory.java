package com.medischool.backend.model.Vaccine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccination_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VaccinationHistory {
    @Id
    @Column(name = "history_id")
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "vaccine_name")
    private String vaccineName;

    @Column(name = "dose_number")
    private Integer doseNumber;

    @Column(name = "vaccination_date")
    private LocalDate vaccinationDate;

    private String location;
    private String note;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "event_id")
    private Integer eventId;
}

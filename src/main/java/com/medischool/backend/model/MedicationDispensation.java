package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "medication_dispensation")
@Getter
@Setter

public class MedicationDispensation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispensation_id")
    private Integer dispensationId;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private MedicationRequest request;

    @Column(name = "nurse_id")
    private UUID nurseId;

    @Column(name = "time")
    private OffsetDateTime time;

    @Column(name = "dosage_given")
    private String dosageGiven;

    @Column(name = "note")
    private String note;

    @Column(name = "is_final_dose")
    private Boolean isFinalDose;
}

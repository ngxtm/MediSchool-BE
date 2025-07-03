package com.medischool.backend.model.medication;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private MedicationRequest request;

    @Column(name = "nurse_id")
    private UUID nurseId;

    @Column(name = "time")
    private OffsetDateTime time;

    @Column(name = "dosage_given")
    private String dosageGiven;

    @Column(name = "note")
    private String note;

}

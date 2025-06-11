package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "medication_request_item")
@Getter
@Setter

public class MedicationRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "medicine_name")
    private String medicineName;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "note")
    private String note;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private MedicationRequest request;
}

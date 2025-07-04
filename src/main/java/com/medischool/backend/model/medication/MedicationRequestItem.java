package com.medischool.backend.model.medication;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    @JsonBackReference("request-items")
    private MedicationRequest request;

    @OneToMany(mappedBy = "item")
    @JsonManagedReference("item-dispensations")
    private List<MedicationDispensation> dispensations;

}

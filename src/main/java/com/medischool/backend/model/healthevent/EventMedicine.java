package com.medischool.backend.model.healthevent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_medicine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventMedicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_medicine_id")
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "medicine_id")
    private Long medicineId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
    private Medicine medicine;

    private Integer quantity;
    private String unit;
    private String note;
}

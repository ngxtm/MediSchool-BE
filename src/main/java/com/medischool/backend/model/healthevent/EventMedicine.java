package com.medischool.backend.model.healthevent;

import jakarta.persistence.*;
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

    private Integer quantity;
    private String unit;
    private String note;
}

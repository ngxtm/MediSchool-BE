package com.medischool.backend.model.healthevent;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medicine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long id;

    @Column(name = "medicine_code")
    private String code;

    @Column(name = "medicine_name")
    private String name;

    private String unit;

    @Column(name = "quantity_on_hand")
    private Integer quantityOnHand;

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    private String note;
}

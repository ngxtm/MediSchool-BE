package com.medischool.backend.model.checkup;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checkup_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
} 
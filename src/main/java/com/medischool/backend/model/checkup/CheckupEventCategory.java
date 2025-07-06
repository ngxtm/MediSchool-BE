package com.medischool.backend.model.checkup;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checkup_event_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupEventCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private CheckupEvent event;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CheckupCategory category;
} 
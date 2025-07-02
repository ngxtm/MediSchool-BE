package com.medischool.backend.model.healthevent;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "student_id")
    private Integer studentId;

    private String problem;
    private String description;
    private String solution;

    @Column(name = "event_time")
    private OffsetDateTime eventTime;

    @Column(name = "record_by")
    private UUID recordBy;

    private String extent;
}

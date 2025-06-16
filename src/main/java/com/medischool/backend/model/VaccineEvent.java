package com.medischool.backend.model;

import com.medischool.backend.model.enums.EventScope;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccine_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "vaccine_id", nullable = false)
    private Integer vaccineId;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_scope")
    @Enumerated(EnumType.STRING)
    private EventScope eventScope;

    private String location;

    @Column(name = "created_by")
    private UUID createdBy;

    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}

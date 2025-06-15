package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccine_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "event_scope")
    private String eventScope;

    private String location;

    @Column(name = "created_by")
    private UUID createdBy;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

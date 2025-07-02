package com.medischool.backend.model.vaccine;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.EventScope;
import com.medischool.backend.model.enums.EventStatus;
import com.medischool.backend.model.vaccine.Vaccine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_scope")
    private EventScope eventScope;

    private String location;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserProfile createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
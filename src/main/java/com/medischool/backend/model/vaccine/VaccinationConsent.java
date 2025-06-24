package com.medischool.backend.model.vaccine;

import com.medischool.backend.model.enums.ConsentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "vaccination_consent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@EntityListeners(VaccinationConsentListener.class)
public class VaccinationConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consent_id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "consent_status", nullable = true)
    @Enumerated(EnumType.STRING)
    private ConsentStatus consentStatus;

    @Column(name = "parent_id")
    private UUID parentId;

    private String note;
}
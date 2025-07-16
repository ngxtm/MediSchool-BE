package com.medischool.backend.model.checkup;

import com.medischool.backend.model.enums.CheckupConsentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkup_category_consent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupCategoryConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "consent_id", nullable = false)
    private CheckupEventConsent consent;

    @ManyToOne
    @JoinColumn(name = "event_category_id", nullable = false)
    private CheckupEventCategory eventCategory;

    @Column(name = "category_consent_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckupConsentStatus categoryConsentStatus;

    @Column(name = "note")
    private String note;
}
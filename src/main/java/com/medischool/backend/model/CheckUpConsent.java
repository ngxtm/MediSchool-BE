package com.medischool.backend.model;

import com.medischool.backend.util.constant.ConsentHeaderStatusType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name="checkup_consent")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckUpConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long consentId;

    Long checkupId;

    Long studentId;

    Long parentId;

    @Enumerated(EnumType.STRING)
    ConsentHeaderStatusType status;

    Long confirmedBy;

    Instant confirmedAt;

    Instant createdAt;

    @ManyToMany
    @JoinTable(
            name = "checkup_consent_item_map",
            joinColumns = @JoinColumn(name = "checkup_consent_id"),
            inverseJoinColumns = @JoinColumn(name = "checkup_item_id")
    )
    Set<CheckUpConsentItem> checkUpConsentItems;


    @ManyToOne
    @JoinColumn(name="periodic_check_up_id")
    PeriodicCheckup periodicCheckup;


    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        confirmedAt = Instant.now();
    }






}

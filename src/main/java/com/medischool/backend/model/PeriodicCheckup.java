package com.medischool.backend.model;


import com.medischool.backend.util.constant.CheckupScopeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name="periodic_checkup")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PeriodicCheckup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long checkUpId;

    @Column(columnDefinition = "TEXT")
    String checkUpTitle;

    Instant scheduleDate;

    @Column(columnDefinition = "TEXT")
    String schoolYear;

    @Enumerated(EnumType.STRING)
    CheckupScopeType  checkupScopeType;

    String status;

    @Column(columnDefinition = "TEXT")
    String text;

    Long createdBy;

    Boolean isDeleted;

    Instant createdAt;

    @OneToMany(mappedBy = "periodicCheckup")
    Set<CheckUpConsent> checkUpConsent;

    @OneToMany(mappedBy = "periodicCheckup")
    Set<CheckUpClass> classes;

    @ManyToMany
    @JoinTable(
            name = "periodic_checkup_item",
            joinColumns = @JoinColumn(name = "periodic_id"),
            inverseJoinColumns = @JoinColumn(name = "checkup_id")
    )
    Set<CheckUpConsentItem> checkUpConsentItems;



    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

}

package com.medischool.backend.model;


import com.medischool.backend.util.constant.ConsentItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name="checkup_consent_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckUpConsentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long itemId;

    Boolean isActive;

    @Column(columnDefinition = "TEXT")
    String text;


    @ManyToMany(mappedBy = "checkUpConsentItems")
    Set<CheckUpConsent> checkUpConsents;

    @ManyToMany(mappedBy = "checkUpConsentItems")
    Set<PeriodicCheckup> periodicCheckups;

    @OneToMany(mappedBy = "checkUpConsentItem")
    Set<CheckUpConsentCategory>  checkUpConsentCategory;



}

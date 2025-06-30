package com.medischool.backend.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name="checkup_consent_category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckUpConsentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String itemCategoryName;

    Boolean isActive;

    @ManyToOne
    @JoinColumn(name="consent_item_id")
    CheckUpConsentItem checkUpConsentItem;
}

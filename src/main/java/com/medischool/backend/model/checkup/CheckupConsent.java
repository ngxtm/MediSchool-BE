package com.medischool.backend.model.checkup;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.parentstudent.Student;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkup_consent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private CheckupEvent event;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private UserProfile parent;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CheckupCategory category;

    @Enumerated(EnumType.STRING)
    private ConsentStatus consentStatus;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "fully_rejected")
    private Boolean fullyRejected = false;
} 
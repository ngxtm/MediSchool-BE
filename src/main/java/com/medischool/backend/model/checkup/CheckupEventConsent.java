package com.medischool.backend.model.checkup;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.model.parentstudent.Student;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checkup_event_consent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupEventConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private CheckupEvent event;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private UserProfile parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_status")
    private CheckupConsentStatus consentStatus;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CheckupCategoryConsent> categoryConsents = new ArrayList<>();
} 
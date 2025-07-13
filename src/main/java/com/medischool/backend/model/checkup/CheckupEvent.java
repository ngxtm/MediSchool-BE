package com.medischool.backend.model.checkup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.CheckupEventScope;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "checkup_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "school_year")
    private String schoolYear;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String status;

    @Enumerated(EnumType.STRING)
    private CheckupEventScope scope;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserProfile createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CheckupEventCategory> eventCategories;
}
package com.medischool.backend.model.checkup;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.CheckupEventScope;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserProfile createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private java.util.List<Long> categoryIds;

    @Enumerated(EnumType.STRING)
    private CheckupEventScope scope;
} 
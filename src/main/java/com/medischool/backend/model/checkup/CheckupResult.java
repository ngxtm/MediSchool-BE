package com.medischool.backend.model.checkup;

import com.medischool.backend.dto.checkup.CheckupResultDTO;
import com.medischool.backend.dto.checkup.CheckupResultItemDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ResultStatus;
import com.medischool.backend.model.parentstudent.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "checkup_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "consent_id", nullable = false)
    private CheckupEventConsent consent;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private CheckupEvent event;

    private String note;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResultStatus status;
    @Column(name = "event_date")
    private LocalDate eventDate;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL)
    private List<CheckupResultItem> resultItems;
} 
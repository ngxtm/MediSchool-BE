package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "medication_request")
@Getter
@Setter

public class MedicationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @ManyToOne
    private Student student;

    @Column(name = "reason")
    private String reason;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "review_by")
    private UUID reviewBy;

    @Column(name = "confirm_by")
    private UUID confirmBy;

    @Column(name = "status")
    private String status;

    @Column(name = "title")
    private String title;

    @Column(name = "note")
    private String note;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Setter
    @Column(name = "create_at")
    private OffsetDateTime createAt;

    @Column(name = "update_at")
    private OffsetDateTime updateAt;

    @Column(name = "is_final_dose")
    private Boolean isFinalDose;

    @OneToMany(mappedBy = "request")
    private List<MedicationRequestItem> items;

    @OneToMany(mappedBy = "request")
    private List<MedicationDispensation> dispensations;
}

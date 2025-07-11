package com.medischool.backend.model.checkup;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ResultStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkup_result_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupResultItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_id", nullable = false)
    private CheckupResult result;

    @ManyToOne
    @JoinColumn(name = "event_category_id", nullable = false)
    private CheckupEventCategory eventCategory;

    @ManyToOne
    @JoinColumn(name = "record_by")
    private UserProfile doctor;

    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResultStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

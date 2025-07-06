package com.medischool.backend.model.checkup;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "event_id")
    private CheckupEvent event;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CheckupCategory category;

    @Column(name = "result_data")
    private String resultData;


    @Column(name = "checked_at")
    private LocalDateTime checkedAt;
} 
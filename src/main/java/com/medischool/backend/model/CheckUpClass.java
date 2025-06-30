package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name="check_up_class")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckUpClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long checkUpClassId;

    String classCode;

    @ManyToOne
    @JoinColumn(name="periodic_check_up_id")
    PeriodicCheckup periodicCheckup;

}

package com.medischool.backend.repository;

import com.medischool.backend.model.Vaccine.VaccineEventClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineEventClassRepository extends JpaRepository<VaccineEventClass, VaccineEventClass.PK> {
    boolean existsByEventIdAndClassCode(Long eventId, String classCode);
}
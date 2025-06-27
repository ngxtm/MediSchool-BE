package com.medischool.backend.repository.vaccination;

import com.medischool.backend.model.vaccine.VaccineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccineEventRepository extends JpaRepository<VaccineEvent, Long> {
    List<VaccineEvent> findAllByEventDateBetween(LocalDate startDate, LocalDate endDate);

    List<VaccineEvent> findAllByEventDateAfter(LocalDate eventDateAfter);
}
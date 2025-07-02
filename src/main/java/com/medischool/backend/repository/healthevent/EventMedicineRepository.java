package com.medischool.backend.repository.healthevent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.healthevent.EventMedicine;

@Repository
public interface EventMedicineRepository extends JpaRepository<EventMedicine, Long> {
    List<EventMedicine> findByEventId(Long eventId);
    List<EventMedicine> findByMedicineId(Long medicineId);
    void deleteByEventId(Long eventId);
} 
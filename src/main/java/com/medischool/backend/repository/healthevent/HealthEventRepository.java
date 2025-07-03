package com.medischool.backend.repository.healthevent;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.healthevent.HealthEvent;

@Repository
public interface HealthEventRepository extends JpaRepository<HealthEvent, Long> {
    List<HealthEvent> findByExtent(String extent);
    
    @EntityGraph(attributePaths = {"student", "eventMedicines", "eventMedicines.medicine"})
    @Query("SELECT h FROM HealthEvent h")
    List<HealthEvent> findAllWithStudent();
    
    @EntityGraph(attributePaths = {"student", "eventMedicines", "eventMedicines.medicine"})
    @Query("SELECT h FROM HealthEvent h WHERE h.extent = ?1")
    List<HealthEvent> findByExtentWithStudent(String extent);
}

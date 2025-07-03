package com.medischool.backend.repository.healthevent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.healthevent.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByNameContainingIgnoreCase(String name);
    List<Medicine> findByQuantityOnHandGreaterThan(Integer threshold);
    List<Medicine> findByQuantityOnHandLessThanEqual(Integer threshold);
} 
package com.medischool.backend.repository;

import com.medischool.backend.model.vaccine.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Integer> {
    List<Vaccine> findAllByCategoryIdIn(List<Integer> categoryIds);
}
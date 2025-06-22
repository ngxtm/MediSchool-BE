package com.medischool.backend.repository;

import com.medischool.backend.model.parentstudent.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
}

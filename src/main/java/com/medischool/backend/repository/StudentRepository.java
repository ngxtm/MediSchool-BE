package com.medischool.backend.repository;

import com.medischool.backend.model.parentstudent.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentId(Integer studentId);
    List<Student> findByClassCodeIn(List<String> classCodes);
    long count();
}

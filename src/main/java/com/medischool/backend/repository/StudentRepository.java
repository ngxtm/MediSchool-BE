package com.medischool.backend.repository;

 checkup-result

import com.medischool.backend.model.StudentProfile;

import com.medischool.backend.model.parentstudent.Student;
 main
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
 checkup-result


@Repository
public interface StudentRepository extends JpaRepository<StudentProfile, Integer> {
 StudentProfile findByStudentCode(String studentCode);
 List<StudentProfile> findByClassCodeIn(List<String> classCode);

    StudentProfile findById(Long studentProfileId);

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentId(Integer studentId);
    List<Student> findByClassCodeIn(List<String> classCodes);
 main
}

package com.medischool.backend.repository;



import com.medischool.backend.model.StudentProfile;

import com.medischool.backend.model.parentstudent.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface StudentRepository extends JpaRepository<StudentProfile, Integer> {
 StudentProfile findByStudentCode(String studentCode);
 List<StudentProfile> findByClassCodeIn(List<String> classCode);

    StudentProfile findByStudentId(Long studentProfileId);

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentId(Integer studentId);
    List<Student> findByClassCodeIn(List<String> classCodes);

}

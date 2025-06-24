package com.medischool.backend.repository;


import com.medischool.backend.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StudentRepository extends JpaRepository<StudentProfile, Integer> {
 StudentProfile findByStudentCode(String studentCode);
 List<StudentProfile> findByClassCodeIn(List<String> classCode);

    StudentProfile findById(Long studentProfileId);
}

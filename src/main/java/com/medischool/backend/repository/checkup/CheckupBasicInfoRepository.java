package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupBasicInfo;
import com.medischool.backend.model.parentstudent.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckupBasicInfoRepository extends JpaRepository<CheckupBasicInfo, Long> {
    Optional<CheckupBasicInfo> findByStudent(Student student);
    Optional<CheckupBasicInfo> findByStudent_StudentId(Integer studentId);
} 
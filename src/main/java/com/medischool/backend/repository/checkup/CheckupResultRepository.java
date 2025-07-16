package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckupResultRepository extends JpaRepository<CheckupResult, Long> {
    List<CheckupResult> findByEvent_IdAndStudent_StudentId(Long eventId, Integer studentId);
    List<CheckupResult> findByEvent_Id(Long eventId);
    List<CheckupResult> findByStudent_StudentId(Integer studentId);
    CheckupResult findByEvent_IdAndStudent_StudentIdAndCategory_Id(Long eventId, Integer studentId, Long categoryId);
} 
package com.medischool.backend.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.model.checkup.CheckupBasicInfo;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.checkup.CheckupBasicInfoRepository;
import com.medischool.backend.service.checkup.CheckupBasicInfoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckupBasicInfoServiceImpl implements CheckupBasicInfoService {
    private final CheckupBasicInfoRepository checkupBasicInfoRepository;
    private final StudentRepository studentRepository;

    @Override
    public CheckupBasicInfo getByStudentId(Integer studentId) {
        return checkupBasicInfoRepository.findByStudent_StudentId(studentId).orElse(null);
    }

    @Override
    @Transactional
    public CheckupBasicInfo updateByStudentId(Integer studentId, CheckupBasicInfo info) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        CheckupBasicInfo existing = checkupBasicInfoRepository.findByStudent(student).orElse(null);
        if (existing == null) {
            info.setStudent(student);
            info.setUpdatedAt(LocalDateTime.now());
            return checkupBasicInfoRepository.save(info);
        } else {
            existing.setBloodType(info.getBloodType());
            existing.setHeight(info.getHeight());
            existing.setWeight(info.getWeight());
            existing.setVisionLeft(info.getVisionLeft());
            existing.setVisionRight(info.getVisionRight());
            existing.setUnderlyingDiseases(info.getUnderlyingDiseases());
            existing.setAllergies(info.getAllergies());
            existing.setUpdatedAt(LocalDateTime.now());
            return checkupBasicInfoRepository.save(existing);
        }
    }
} 
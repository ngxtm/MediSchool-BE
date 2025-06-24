package com.medischool.backend.service;

import com.medischool.backend.dto.student.StudentDetailDTO;

public interface StudentService {
    StudentDetailDTO getStudentDetail(Integer id);
}

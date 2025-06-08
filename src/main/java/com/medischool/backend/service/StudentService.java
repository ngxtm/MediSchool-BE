package com.medischool.backend.service;

import com.medischool.backend.dto.StudentDetailDTO;

public interface StudentService {
    StudentDetailDTO getStudentDetail(Integer id);
}

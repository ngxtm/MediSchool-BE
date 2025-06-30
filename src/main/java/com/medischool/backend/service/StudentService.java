package com.medischool.backend.service;

import com.medischool.backend.dto.student.StudentDetailDTO;
import com.medischool.backend.model.parentstudent.Student;

import java.util.List;

public interface StudentService {
    StudentDetailDTO getStudentDetail(Integer id);

    List<Student> getAllStudents();
}

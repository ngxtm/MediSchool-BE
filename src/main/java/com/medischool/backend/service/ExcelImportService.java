package com.medischool.backend.service;

import com.medischool.backend.dto.student.StudentImportResponseDTO;
import com.medischool.backend.model.parentstudent.Student;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelImportService {
    StudentImportResponseDTO importStudentsFromExcel(MultipartFile file);
    byte[] generateStudentListExcel(List<Student> students);
} 
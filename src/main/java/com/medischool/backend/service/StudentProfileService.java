package com.medischool.backend.service;

import com.medischool.backend.dto.response.StudentProfileResponse;
import com.medischool.backend.model.StudentProfile;
import com.medischool.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentProfileService {
    private final StudentRepository studentRepository;
    public StudentProfileResponse findByStudentCode(String code){
        StudentProfile studentProfile = studentRepository.findByStudentCode(code);
        StudentProfileResponse studentProfileResponse = new StudentProfileResponse();
        studentProfileResponse.setStudentCode(code);
        studentProfileResponse.setStudentName(studentProfile.getName());
        studentProfileResponse.setClassCode(studentProfile.getClassCode());
        return studentProfileResponse;
    }
}

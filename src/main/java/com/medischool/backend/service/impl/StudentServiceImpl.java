package com.medischool.backend.service.impl;

import com.medischool.backend.dto.StudentDetailDTO;
import com.medischool.backend.model.ParentStudentLink;
import com.medischool.backend.model.Student;
import com.medischool.backend.model.enums.Relationship;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public StudentDetailDTO getStudentDetail(Integer id) {
        Student student = studentRepository.findByStudentId(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy học sinh"));

        List<ParentStudentLink> links = linkRepository.findByStudentId(id);
        String fatherName="", motherName="", motherPhone="", fatherPhone="";

        for (ParentStudentLink link : links) {
            var profile = userProfileRepository.findById(link.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phụ huynh"));
            if ("FATHER".equalsIgnoreCase(link.getRelationship().toString())) {
                fatherName = profile.getFullName();
                fatherPhone = profile.getPhone();
            }
            if ("MOTHER".equalsIgnoreCase(link.getRelationship().toString())) {
                motherName = profile.getFullName();
                motherPhone = profile.getPhone();
            }
        }

        return new StudentDetailDTO(
                student.getFullName(),
                student.getStudentCode(),
                student.getGender(),
                student.getDateOfBirth(),
                student.getClassCode(),
                student.getEnrollmentDate(),
                fatherName, fatherPhone,
                motherName, motherPhone,
                student.getEmergencyContact(),
                student.getEmergencyPhone(),
                student.getAddress()
        );
    }
}

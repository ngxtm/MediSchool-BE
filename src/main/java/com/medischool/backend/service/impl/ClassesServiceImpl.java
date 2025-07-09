package com.medischool.backend.service.impl;

import com.medischool.backend.model.parentstudent.Classes;
import com.medischool.backend.repository.ClassesRepository;
import com.medischool.backend.service.ClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassesServiceImpl implements ClassesService {

    private final ClassesRepository classesRepository;

    @Override
    public List<Classes> findAll() {
        return classesRepository.findAll();
    }

    @Override
    public List<String> findAllGrades() {
        return findAll().stream()
                .map(Classes::getName)
                .map(code -> code.split("\\.")[0]) // lấy phần trước dấu chấm
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Classes> findByGrade(String grade) {
        return findAll().stream()
                .filter(c -> c.getName().startsWith(grade + "."))
                .collect(Collectors.toList());
    }

}

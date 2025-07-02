package com.medischool.backend.service.impl;

import com.medischool.backend.model.parentstudent.Classes;
import com.medischool.backend.repository.ClassesRepository;
import com.medischool.backend.service.ClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassesServiceImpl implements ClassesService {

    private final ClassesRepository classesRepository;

    @Override
    public List<Classes> findAll() {
        return classesRepository.findAll();
    }
}

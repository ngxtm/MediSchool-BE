package com.medischool.backend.service;

import com.medischool.backend.model.parentstudent.Classes;

import java.util.List;

public interface ClassesService {
    public List<Classes> findAll();

    public List<String> findAllGrades();

    public List<Classes> findByGrade(String grade);

}

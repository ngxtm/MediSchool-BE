package com.medischool.backend.service;


import com.medischool.backend.model.CheckUpClass;
import com.medischool.backend.model.PeriodicCheckup;
import com.medischool.backend.repository.CheckupClassRepository;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckupClassService {
    private final CheckupClassRepository checkupClassRepository;
    public Set<CheckUpClass> saveCheckupClass(List<String> classesCode, PeriodicCheckup periodicCheckup) throws CustomException {
        Set<CheckUpClass> checkUpClasses=new HashSet<>();
        for(String classCode:classesCode){
            CheckUpClass checkUpClass = new CheckUpClass();
            checkUpClass.setClassCode(classCode);
            checkUpClass.setPeriodicCheckup(periodicCheckup);
            this.checkupClassRepository.save(checkUpClass);
            checkUpClasses.add(checkUpClass);
        }
        return checkUpClasses;
    }
}

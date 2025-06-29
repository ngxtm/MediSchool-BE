package com.medischool.backend.service;

import com.medischool.backend.dto.request.PeriodicCheckupRequest;
import com.medischool.backend.dto.response.PeriodicCheckupResponse;
import com.medischool.backend.helper.PeriodicCheckupServiceHelper;
import com.medischool.backend.model.*;
import com.medischool.backend.repository.CheckupClassRepository;
import com.medischool.backend.repository.CheckupConsentItemRepository;
import com.medischool.backend.repository.PeriodicCheckupRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.util.constant.CheckupScopeType;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PeriodicCheckupService {
    //dependency injection
    private final PeriodicCheckupRepository periodicCheckupRepository;
    private final PeriodicCheckupServiceHelper periodicCheckupServiceHelper;
    private final StudentRepository studentRepository;
    private final CheckupConsentService checkupConsentService;
    private final CheckupClassService checkupClassService;
    private final CheckupConsentItemRepository consentItemRepository;

    public String getYearOfInstant(Instant year){
        LocalDateTime dateTime = LocalDateTime.ofInstant(year, ZoneId.systemDefault());
        int currentYear = dateTime.getYear();
        return String.valueOf(currentYear);
    }
    public PeriodicCheckupResponse savePeriodicCheckup(PeriodicCheckupRequest periodicCheckupRequest) throws CustomException {
        this.periodicCheckupServiceHelper.checkValidInfoCreatePeriodicCheckup(periodicCheckupRequest);
        PeriodicCheckup periodicCheckup = new PeriodicCheckup();
        if(periodicCheckupRequest.getPeriodicCheckupId()!=null){
            periodicCheckup=this.periodicCheckupRepository.findById(periodicCheckupRequest.getPeriodicCheckupId()).orElse(null);
        }
        periodicCheckup.setCheckupScopeType(periodicCheckupRequest.getScope());
        periodicCheckup.setText(periodicCheckupRequest.getText());
        periodicCheckup.setCheckUpTitle(periodicCheckupRequest.getPeriodicTitle());
        periodicCheckup.setScheduleDate(this.periodicCheckupServiceHelper.convertScheduleDateToInstant(periodicCheckupRequest.getPeriodicDate()));
        periodicCheckup.setStatus(periodicCheckupRequest.getStatus());
        periodicCheckup.setIsDeleted(false);
        periodicCheckup.setSchoolYear(periodicCheckupRequest.getSchoolYear());
        PeriodicCheckup currentPeriodicCheckup=this.periodicCheckupRepository.save(periodicCheckup);
        Set<CheckUpConsentItem> checkUpConsentItems=new HashSet<>();

        //Tìm mục khám theo danh sách id đã gửi về, rồi thêm vào set
        for(Long items:periodicCheckupRequest.getIdsCheckupConsent()){
            CheckUpConsentItem checkUpConsentItem=this.consentItemRepository.findById(items).orElse(null);
            checkUpConsentItems.add(checkUpConsentItem);
        }

        List<StudentProfile> allStudents=this.studentRepository.findAll();
        this.checkupConsentService.saveCheckupConsent(allStudents,currentPeriodicCheckup);

        //set lại danh sách mục khám vào kì khám hiện tai
        currentPeriodicCheckup.setCheckUpConsentItems(checkUpConsentItems);

//        if(periodicCheckupRequest.getScope().equals(CheckupScopeType.CLASS)){
//            List<StudentProfile> students=this.studentRepository.findByClassCodeIn(periodicCheckupRequest.getClassCode());
//            this.checkupConsentService.saveCheckupConsent(students,currentPeriodicCheckup);
//            Set<CheckUpClass> classes=this.checkupClassService.saveCheckupClass(periodicCheckupRequest.getClassCode(),currentPeriodicCheckup);
//            currentPeriodicCheckup.setClasses(classes);
//        }else{
//            List<StudentProfile> allStudents=this.studentRepository.findAll();
//            this.checkupConsentService.saveCheckupConsent(allStudents,currentPeriodicCheckup);
//        }

        this.periodicCheckupRepository.save(currentPeriodicCheckup);
        PeriodicCheckupResponse periodicCheckupResponse = this.periodicCheckupServiceHelper.convertToPeriodicCheckupResponse(currentPeriodicCheckup);
        return periodicCheckupResponse;
    }


    public void deletePeriodicCheckup(Long periodicCheckupId) throws CustomException {
        PeriodicCheckup periodicCheckup=this.periodicCheckupRepository.findById(periodicCheckupId).orElse(null);
        if(periodicCheckup==null){
            throw new CustomException("PeriodicCheckup not found");
        }

        if(periodicCheckup.getIsDeleted()==true){
            periodicCheckup.setIsDeleted(false);
        }else{
            periodicCheckup.setIsDeleted(true);
        }

        this.periodicCheckupRepository.save(periodicCheckup);
    }


    public PeriodicCheckupResponse getPeriodicCheckupById(Long periodicCheckupId) throws CustomException {
        PeriodicCheckup periodicCheckup = this.periodicCheckupRepository.findById(periodicCheckupId).orElse(null);
        if(periodicCheckup==null){
            throw new CustomException("PeriodicCheckup not found");
        }
        PeriodicCheckupResponse periodicCheckupResponse=this.periodicCheckupServiceHelper.convertToPeriodicCheckupResponse(periodicCheckup);
        return periodicCheckupResponse;
    }

    public List<PeriodicCheckupResponse> getPeriodicCheckupByYear(String year) throws CustomException {
        List<PeriodicCheckup> periodicCheckups=this.periodicCheckupRepository.findAll();
        List<PeriodicCheckupResponse> periodicCheckupResponses=new ArrayList<>();
        for(PeriodicCheckup periodicCheckup:periodicCheckups){
            String currentYear=this.getYearOfInstant(periodicCheckup.getScheduleDate());
            PeriodicCheckupResponse periodicCheckupResponse=new PeriodicCheckupResponse();
            if(currentYear.equals(year)){
                periodicCheckupResponse=this.periodicCheckupServiceHelper.convertToPeriodicCheckupResponse(periodicCheckup);
                periodicCheckupResponses.add(periodicCheckupResponse);
            }
        }
        return periodicCheckupResponses;
    }
}

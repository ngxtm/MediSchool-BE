package com.medischool.backend.helper;


import com.medischool.backend.dto.request.PeriodicCheckupRequest;
import com.medischool.backend.dto.response.PeriodicCheckupResponse;
import com.medischool.backend.model.CheckUpClass;
import com.medischool.backend.model.CheckUpConsentItem;
import com.medischool.backend.model.PeriodicCheckup;
import com.medischool.backend.repository.CheckupClassRepository;
import com.medischool.backend.repository.PeriodicCheckupRepository;
import com.medischool.backend.util.constant.CheckupScopeType;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PeriodicCheckupServiceHelper {
    private final PeriodicCheckupRepository periodicCheckupRepository;
    private final CheckupClassRepository checkupClassRepository;
    public void checkValidInfoCreatePeriodicCheckup(PeriodicCheckupRequest periodicCheckupRequest) throws CustomException {
        if(periodicCheckupRequest.getPeriodicTitle()==null || periodicCheckupRequest.getPeriodicTitle().isEmpty()){
            throw new CustomException("Title Required");
        }
        if(periodicCheckupRequest.getSchoolYear()==null || periodicCheckupRequest.getSchoolYear().isEmpty()){
            throw new CustomException("School Year Required");
        }
        if(periodicCheckupRequest.getPeriodicDate()==null || periodicCheckupRequest.getPeriodicDate().isEmpty()){
            throw new CustomException("Periodic Date Required");
        }
        if(periodicCheckupRequest.getStatus()==null || periodicCheckupRequest.getStatus().isEmpty()){
            throw new CustomException("Status Required");
        }
        if(periodicCheckupRequest.getScope()==null){
            throw new CustomException("Scope Required");
        }
        if(periodicCheckupRequest.getIdsCheckupConsent()==null || periodicCheckupRequest.getIdsCheckupConsent().isEmpty()){
            throw new CustomException("Ids Checkup Consent Required");
        }
        if(periodicCheckupRequest.getScope()!=null){
            if(periodicCheckupRequest.getScope().equals(CheckupScopeType.CLASS) && periodicCheckupRequest.getClassCode()==null){
                throw new CustomException("Class code required");
            }
        }
    }

    public Instant convertScheduleDateToInstant(String scheduleDate) throws CustomException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(scheduleDate, formatter);
        Instant instant = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
       return instant;
    }

    public PeriodicCheckupResponse convertToPeriodicCheckupResponse(PeriodicCheckup periodicCheckup) throws CustomException {
        PeriodicCheckupResponse periodicCheckupResponse = new PeriodicCheckupResponse();
        periodicCheckupResponse.setCheckUpId(periodicCheckup.getCheckUpId());
        periodicCheckupResponse.setCheckupScopeType(periodicCheckup.getCheckupScopeType());
        periodicCheckupResponse.setText(periodicCheckup.getText());
        periodicCheckupResponse.setCheckUpTitle(periodicCheckup.getCheckUpTitle());
        periodicCheckupResponse.setCreatedAt(periodicCheckup.getCreatedAt());
        periodicCheckupResponse.setCreatedBy(periodicCheckup.getCreatedBy());
        periodicCheckupResponse.setScheduleDate(periodicCheckup.getScheduleDate());
        periodicCheckupResponse.setSchoolYear(periodicCheckup.getSchoolYear());
        periodicCheckupResponse.setStatus(periodicCheckup.getStatus());
        periodicCheckupResponse.setIsDeleted(periodicCheckup.getIsDeleted());
        List<String> classesCode=new ArrayList<>();
        List<String> checkUpConsentItemNames=new ArrayList<>();
        if(periodicCheckup.getCheckupScopeType().equals(CheckupScopeType.CLASS)){
            for(CheckUpClass classCode:periodicCheckup.getClasses()){
                CheckUpClass currentClassCode=this.checkupClassRepository.findById(classCode.getCheckUpClassId()).orElse(null);
                classesCode.add(currentClassCode.getClassCode());
            }
        }
        for(CheckUpConsentItem checkUpItemsName: periodicCheckup.getCheckUpConsentItems()){
            checkUpConsentItemNames.add(checkUpItemsName.getText());
        }
        periodicCheckupResponse.setItemsName(checkUpConsentItemNames);
        periodicCheckupResponse.setClassCode(classesCode);
        return periodicCheckupResponse;

    }
}

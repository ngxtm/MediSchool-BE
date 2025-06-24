package com.medischool.backend.service;


import com.medischool.backend.dto.request.CheckupResultRequest;
import com.medischool.backend.dto.response.CheckupResultResponse;
import com.medischool.backend.helper.CheckupResultServiceHelper;
import com.medischool.backend.model.CheckupResult;
import com.medischool.backend.model.PeriodicCheckup;
import com.medischool.backend.model.StudentProfile;
import com.medischool.backend.repository.CheckupResultRepository;
import com.medischool.backend.repository.PeriodicCheckupRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckupResultService {
    private final CheckupResultRepository checkupResultRepository;
    private final CheckupResultServiceHelper  checkupResultServiceHelper;
    private final PeriodicCheckupRepository periodicCheckupRepository;
    private final StudentRepository studentRepository;
    public CheckupResultResponse saveCheckupResult(CheckupResultRequest checkupResultRequest) throws CustomException {
        this.checkupResultServiceHelper.checkInvaliInfo(checkupResultRequest);
        CheckupResult checkupResult = new CheckupResult();
        checkupResult.setResult(checkupResultRequest.getResult());
        checkupResult.setNote(checkupResultRequest.getNote());
        checkupResult.setIsNormal(checkupResultRequest.getIsNormal());


        PeriodicCheckup currentPeriodicCheckup =this.periodicCheckupRepository.findById(checkupResultRequest.getPeriodicCheckupId()).get();
        checkupResult.setPeriodicCheckup(currentPeriodicCheckup);
        checkupResult.setNurseId(checkupResultRequest.getNurseId());


        StudentProfile currentStudent=this.studentRepository.findById(Math.toIntExact(checkupResultRequest.getStudentProfileId())).get();
        checkupResult.setStudentProfile(currentStudent);
        this.checkupResultRepository.save(checkupResult);
        CheckupResultResponse checkupResultResponse = this.checkupResultServiceHelper.convertToResultResponse(checkupResult);
        return checkupResultResponse;



    }


}

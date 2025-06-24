package com.medischool.backend.helper;


import com.medischool.backend.dto.request.CheckupResultRequest;
import com.medischool.backend.dto.response.CheckupResultResponse;
import com.medischool.backend.model.CheckupResult;
import com.medischool.backend.model.PeriodicCheckup;
import com.medischool.backend.repository.CheckupResultRepository;
import com.medischool.backend.repository.PeriodicCheckupRepository;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckupResultServiceHelper {

    private final CheckupResultRepository checkupResultRepository;
    private final PeriodicCheckupRepository periodicCheckupRepository;

    public void checkInvaliInfo(CheckupResultRequest checkupResultRequest) throws CustomException {
        if(checkupResultRequest.getResult()==null){
            throw new CustomException("Result is null");
        }
        if(checkupResultRequest.getNote()==null){
            throw new CustomException("Note is null");
        }
        if(checkupResultRequest.getPeriodicCheckupId()==null){
            throw new CustomException("PeriodicCheckupId is null");
        }
        if(checkupResultRequest.getNurseId()==null){
            throw new CustomException("NurseId is null");
        }

        if(checkupResultRequest.getStudentProfileId()==null){
            throw new CustomException("StudentProfileId is null");
        }
    }
    public CheckupResultResponse convertToResultResponse(CheckupResult checkupResult) throws CustomException {
        CheckupResultResponse checkupResultResponse = new CheckupResultResponse();
        checkupResultResponse.setResult(checkupResult.getResult());
        checkupResultResponse.setNote(checkupResult.getNote());
        checkupResultResponse.setIsNormal(checkupResult.getIsNormal());

        checkupResultResponse.setPeriodicName(checkupResult.getPeriodicCheckup().getCheckUpTitle());
        checkupResultResponse.setStudentName(checkupResult.getStudentProfile().getName());
        checkupResultResponse.setStudentCode(checkupResult.getStudentProfile().getStudentCode());
        return checkupResultResponse;
    }
}

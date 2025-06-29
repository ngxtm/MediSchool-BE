package com.medischool.backend.service;


import com.medischool.backend.dto.request.CheckupItemEntryRequest;
import com.medischool.backend.dto.request.CheckupItemRequest;
import com.medischool.backend.dto.response.CheckupConsentResponse;
import com.medischool.backend.helper.CheckupConsentServiceHelper;
import com.medischool.backend.model.*;
import com.medischool.backend.repository.CheckupConsentItemRepository;
import com.medischool.backend.repository.CheckupConsentRepository;
import com.medischool.backend.util.constant.ConsentHeaderStatusType;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckupConsentService {
    private final CheckupConsentRepository checkupConsentRepository;
    private final CheckupConsentServiceHelper checkupConsentServiceHelper;
    private final CheckupConsentItemRepository consentItemRepository;
    public void saveCheckupConsent(List<StudentProfile> students, PeriodicCheckup periodicCheckup){
        for(StudentProfile student:students){
            CheckUpConsent checkupConsent=new CheckUpConsent();
            StudentProfile currentStudent=student;
            checkupConsent.setStudentId(student.getStudentId());
            checkupConsent.setStatus(ConsentHeaderStatusType.PENDING_RESPONSE);
            checkupConsent.setPeriodicCheckup(periodicCheckup);
            Set<ParentProfile> parents=student.getParents();
            for(ParentProfile parent:parents){
                checkupConsent.setParentId(parent.getParentId());
                this.checkupConsentRepository.save(checkupConsent);
            }
        }
    }

    public void changeStatusOfConsent(Long consentId) throws CustomException{
        CheckUpConsent checkUpConsent=this.checkupConsentRepository.findById(consentId).orElse(null);
        if(checkUpConsent == null){
            throw new CustomException("ConsentId not found");
        }

        if(checkUpConsent.getStatus().equals(ConsentHeaderStatusType.PENDING_RESPONSE)){
            checkUpConsent.setStatus(ConsentHeaderStatusType.COMPLETED);
        }else{
            checkUpConsent.setStatus(ConsentHeaderStatusType.PENDING_RESPONSE);
        }

        this.checkupConsentRepository.save(checkUpConsent);
    }

    public CheckupConsentResponse acceptCheckupConsent(CheckupItemRequest checkupItemRequest) throws CustomException{
        this.checkupConsentServiceHelper.checkValidInfoAccepted(checkupItemRequest);
        List<CheckupItemEntryRequest> checkupItemRequests=checkupItemRequest.getCheckupItems();
        CheckupConsentResponse checkupConsentResponse=new CheckupConsentResponse();
        for(CheckupItemEntryRequest checkupItemEntryRequest:checkupItemRequests){
            Optional<CheckUpConsent> checkUpConsent=this.checkupConsentRepository.findById(checkupItemEntryRequest.getCheckupId());
            Set<CheckUpConsentItem> checkUpConsentItems=new HashSet<>();
            for (Long itemId : checkupItemEntryRequest.getItemIds()) {
                Optional<CheckUpConsentItem> optionalItem = this.consentItemRepository.findById(itemId);
                optionalItem.ifPresent(checkUpConsentItems::add);
            }

            checkUpConsent.get().setCheckUpConsentItems(checkUpConsentItems);
            checkUpConsent.get().setStatus(ConsentHeaderStatusType.COMPLETED);
           this.checkupConsentRepository.save(checkUpConsent.get());

        }
        checkupConsentResponse=this.checkupConsentServiceHelper.convertToCheckupConsentResponse(checkupItemRequest);
        return checkupConsentResponse;
    }
}


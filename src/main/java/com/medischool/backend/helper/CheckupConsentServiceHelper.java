package com.medischool.backend.helper;


import com.medischool.backend.dto.request.CheckupItemEntryRequest;
import com.medischool.backend.dto.request.CheckupItemRequest;
import com.medischool.backend.dto.response.CheckupConsentEntryResponse;
import com.medischool.backend.dto.response.CheckupConsentResponse;
import com.medischool.backend.dto.response.StudentProfileResponse;
import com.medischool.backend.model.CheckUpConsent;
import com.medischool.backend.model.CheckUpConsentItem;
import com.medischool.backend.model.PeriodicCheckup;
import com.medischool.backend.model.StudentProfile;
import com.medischool.backend.repository.CheckupConsentItemRepository;
import com.medischool.backend.repository.CheckupConsentRepository;
import com.medischool.backend.repository.PeriodicCheckupRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CheckupConsentServiceHelper {
    private final CheckupConsentRepository checkupConsentRepository;
    private final CheckupConsentItemRepository checkupConsentItemRepository;
    private final StudentRepository studentRepository;
    private final PeriodicCheckupRepository periodicCheckupRepository;
    public void checkValidInfoAccepted(CheckupItemRequest checkupItemRequest) throws CustomException {
        List<CheckupItemEntryRequest> checkupItemEntryRequests=checkupItemRequest.getCheckupItems();
        for(CheckupItemEntryRequest checkupItemEntryRequest:checkupItemEntryRequests){

            Long consentId=checkupItemEntryRequest.getCheckupId();
            Optional<CheckUpConsentItem> checkUpConsent=this.checkupConsentItemRepository.findById(consentId);

            if(checkUpConsent.isEmpty()){
                throw new CustomException("ConsentId = "+ consentId+" not found");
            }


            for(Long itemId:checkupItemEntryRequest.getItemIds()){
                CheckUpConsentItem checkUpConsentItem=this.checkupConsentItemRepository.findById(itemId).get();
                if(checkUpConsentItem==null){
                    throw new CustomException("Check up consent item with id = "+itemId+" not found");
                }

                Set<PeriodicCheckup> currentPeriodics=checkUpConsent.get().getPeriodicCheckups();
                Set<String> itemsNotInclude=new HashSet<>();
                for(PeriodicCheckup periodicCheckup:currentPeriodics){
                    Set<CheckUpConsentItem> checkUpConsentItems=periodicCheckup.getCheckUpConsentItems();
                    if(!checkUpConsentItems.contains(checkUpConsentItem)){
                       itemsNotInclude.add(checkUpConsentItem.getText());
                    }
                }
                String itemText="Mục khám :";
                for(String itemNotInclue:itemsNotInclude){
                    itemText+=itemNotInclue+", ";
                }
                itemText+="không nằm trong kì khám";
                if(itemsNotInclude.size()>0){
                    throw new CustomException(itemText);
                }

            }
        }


    }

    public CheckupConsentResponse convertToCheckupConsentResponse(CheckupItemRequest checkupItemRequest) throws CustomException {
        CheckupConsentResponse checkupConsentResponses=new CheckupConsentResponse();
        List<CheckupConsentEntryResponse> checkupConsentEntryResponses=new ArrayList<>();
       for(CheckupItemEntryRequest checkupItemEntryRequest:checkupItemRequest.getCheckupItems()){
           CheckupConsentEntryResponse checkupConsentEntryResponse=new CheckupConsentEntryResponse();
           CheckUpConsent checkUpConsent=this.checkupConsentRepository.findById(checkupItemEntryRequest.getCheckupId()).get();
           StudentProfile currentStudent=this.studentRepository.findById(Math.toIntExact(checkUpConsent.getStudentId())).get();

           PeriodicCheckup currentPeriodic=this.periodicCheckupRepository.findById(checkUpConsent.getPeriodicCheckup().getCheckUpId()).get();

           List<String> itemsInCheckupAccepted=new ArrayList<>();
           Set<CheckUpConsentItem> currentCheckupItems=currentPeriodic.getCheckUpConsentItems();
           for(CheckUpConsentItem checkUpConsentItem:currentCheckupItems){
               itemsInCheckupAccepted.add(checkUpConsentItem.getText());
           }

           checkupConsentEntryResponse.setStudentCode(currentStudent.getStudentCode());
           checkupConsentEntryResponse.setStudentName(currentStudent.getName());
           checkupConsentEntryResponse.setCheckUpItems(itemsInCheckupAccepted);
           checkupConsentEntryResponses.add(checkupConsentEntryResponse);
       }
        checkupConsentResponses.setCheckupConsentEntries(checkupConsentEntryResponses);
        return checkupConsentResponses;

    }
}

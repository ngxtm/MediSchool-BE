package com.medischool.backend.service.checkup;

import com.medischool.backend.dto.checkup.CheckupResultDTO;
import com.medischool.backend.dto.checkup.CheckupResultItemDTO;
import com.medischool.backend.dto.checkup.CheckupResultUpdateDTO;
import com.medischool.backend.model.checkup.CheckupResult;
import java.util.List;

public interface CheckupResultService {
//    boolean isApproved(Long eventId, Integer studentId, Long categoryId);
//    void saveResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt);
//    void updateResult(Long eventId, Integer studentId, Long categoryId, String resultData);
//    void updateResultById(Long resultId, String resultData);
//    List<CheckupResult> getResultsForStudentInEvent(Long eventId, Integer studentId);

    CheckupResultDTO getResultDetail(Long resultId);
    List<CheckupResultDTO> getResultsByEventId(Long eventId);
    List<CheckupResultDTO> getResultsByStudentId(Integer studentId);
    CheckupResultItemDTO updateResultItem(Long itemId, CheckupResultUpdateDTO dto);
    CheckupResultDTO convertToDTO(CheckupResult checkupResult);

//    public interface CheckupResultService {
//        boolean isApproved(Long eventId, Integer studentId, Long categoryId);
//        void saveResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt);
//        void updateResult(Long eventId, Integer studentId, Long categoryId, String resultData);
//        void updateResultById(Long resultId, String resultData);
//        List<CheckupResult> getResultsForStudentInEvent(Long eventId, Integer studentId);
//        void upsertResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt);
//    }
//
//    package com.medischool.backend.service.checkup;
//
//    public class SendConsentResult {
//        public int success;
//        public int failed;
//        public SendConsentResult(int success, int failed) {
//            this.success = success;
//            this.failed = failed;
//        }
//    }
} 
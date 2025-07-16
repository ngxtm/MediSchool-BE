package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.dto.checkup.CheckupOverallResultDTO;
import com.medischool.backend.dto.checkup.CheckupResultDTO;
import com.medischool.backend.dto.checkup.CheckupResultItemDTO;
import com.medischool.backend.dto.checkup.CheckupResultUpdateDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.checkup.*;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.enums.ResultStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.checkup.*;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.service.checkup.CheckupResultService;
import com.medischool.backend.service.checkup.CheckupBasicInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckupResultServiceImpl implements CheckupResultService {
    private final CheckupResultRepository checkupResultRepository;
    private final CheckupResultItemRepository checkupResultItemRepository;
    private final CheckupConsentRepository checkupConsentRepository;

    @Override
    public CheckupResultDTO getResultDetail(Long resultId) {
        CheckupResult result = checkupResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        CheckupEvent event = result.getEvent();
        Student student = result.getStudent();
        CheckupEventConsent consent = result.getConsent(); // Quan trọng!

        List<CheckupResultItem> items = checkupResultItemRepository.findByResultId(resultId);
        List<CheckupResultItemDTO> itemDTOs = items.stream()
                .map(CheckupResultItemDTO::new)
                .collect(Collectors.toList());

        return new CheckupResultDTO(
                result.getId(),

                // Event info
                event.getEventTitle(),
                event.getSchoolYear(),
                event.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

                // Student info
                student.getFullName(),
                student.getStudentCode(),
                student.getClassCode(),
                student.getGender().name(),
                student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

                // Parent info
                (consent != null && consent.getParent() != null) ? consent.getParent().getFullName() : null,
                (consent != null && consent.getParent() != null) ? consent.getParent().getEmail() : null,
                (consent != null && consent.getParent() != null) ? consent.getParent().getPhone() : null,

                // Category items
                itemDTOs
        );
    }

    @Override
    public CheckupResultDTO convertToDTO(CheckupResult result) {
        Student student = result.getStudent();
        CheckupEvent event = result.getEvent();
        CheckupEventConsent consent = result.getConsent();

        return new CheckupResultDTO(
                result.getId(),

                event.getEventTitle(),
                event.getSchoolYear(),
                event.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

                student.getFullName(),
                student.getStudentCode(),
                student.getClassCode(),
                student.getGender().name(),
                student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

                (consent != null && consent.getParent() != null) ? consent.getParent().getFullName() : null,
                (consent != null && consent.getParent() != null) ? consent.getParent().getEmail() : null,
                (consent != null && consent.getParent() != null) ? consent.getParent().getPhone() : null,

                result.getResultItems().stream().map(CheckupResultItemDTO::new).collect(Collectors.toList())
        );
    }

    @Override
    public List<CheckupResultDTO> getResultsByEventId(Long eventId) {
        List<CheckupResult> results = checkupResultRepository.findByEventId(eventId);

        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CheckupResultDTO> getResultsByStudentId(Integer studentId) {
        List<CheckupResult> results = checkupResultRepository.findByStudent_StudentId(studentId);

        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CheckupResultDTO updateOverallResult(Long resultId, CheckupOverallResultDTO dto) {
        CheckupResult result = checkupResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        CheckupEventConsent consent = result.getConsent();
        if (consent == null) {
            throw new RuntimeException("Consent not found for this result");
        }

        if (dto.getStatus() != null) {
            consent.setConsentStatus(CheckupConsentStatus.valueOf(dto.getStatus()));
        }

        consent.setNote(dto.getNote());
        consent.setUpdatedAt(LocalDateTime.now());

        checkupConsentRepository.save(consent);

        // Trả về DTO đã cập nhật
        return convertToDTO(result);
    }

    public CheckupResultItemDTO updateResultItem(Long itemId, CheckupResultUpdateDTO dto) {
        CheckupResultItem item = checkupResultItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Result item not found"));

        item.setValue(dto.getValue());
        item.setStatus(ResultStatus.valueOf(dto.getStatus()));

        checkupResultItemRepository.save(item);
        return new CheckupResultItemDTO(item);
    }


}

//    @Override
//    public boolean isApproved(Long eventId, Integer studentId, Long categoryId) {
//        return checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId)
//            .stream()
//            .anyMatch(c -> c.getCategory().getId().equals(categoryId)
//                && c.getConsentStatus() != null
//                && c.getConsentStatus() == ConsentStatus.APPROVED);
//    }
//
//    @Override
//    public void saveResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt) {
//        CheckupResult result = CheckupResult.builder()
//            .event(checkupEventRepository.findById(eventId).orElseThrow())
//            .student(studentRepository.findById(studentId).orElseThrow())
//            .category(checkupCategoryRepository.findById(categoryId).orElseThrow())
//            .resultData(resultData)
//            .checkedAt(java.time.LocalDateTime.now())
//            .build();
//        checkupResultRepository.save(result);
//    }
//
//    @Override
//    public List<CheckupResult> getResultsForStudentInEvent(Long eventId, Integer studentId) {
//        return checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
//    }
//
//    @Override
//    public void updateResult(Long eventId, Integer studentId, Long categoryId, String resultData) {
//        List<CheckupResult> results = checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
//        CheckupResult result = results.stream()
//            .filter(r -> r.getCategory().getId().equals(categoryId))
//            .findFirst()
//            .orElseThrow(() -> new RuntimeException("Checkup result not found"));
//
//        result.setResultData(resultData);
//        result.setCheckedAt(java.time.LocalDateTime.now());
//        checkupResultRepository.save(result);
//    }
//
//    @Override
//    public void updateResultById(Long resultId, String resultData) {
//        CheckupResult result = checkupResultRepository.findById(resultId)
//            .orElseThrow(() -> new RuntimeException("Checkup result not found with ID: " + resultId));
//
//        result.setResultData(resultData);
//        result.setCheckedAt(java.time.LocalDateTime.now());
//        checkupResultRepository.save(result);
//
//        // Đồng thời cập nhật CheckupBasicInfo nếu là category cơ bản
//        Long categoryId = result.getCategory().getId();
//        Integer studentId = result.getStudent().getStudentId();
//        if (categoryId != null && studentId != null) {
//            CheckupBasicInfo info = checkupBasicInfoService.getByStudentId(studentId);
//            if (info == null) {
//                info = CheckupBasicInfo.builder().build();
//            }
//            boolean updated = false;
//            switch (categoryId.intValue()) {
//                case 1:
//                    try { info.setHeight(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 2:
//                    try { info.setWeight(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 3:
//                    info.setBloodType(resultData); updated = true;
//                    break;
//                case 4:
//                    try { info.setVisionLeft(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 5:
//                    try { info.setVisionRight(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 6:
//                    info.setUnderlyingDiseases(resultData); updated = true;
//                    break;
//                case 7:
//                    info.setAllergies(resultData); updated = true;
//                    break;
//                default:
//                    break;
//            }
//            if (updated) {
//                checkupBasicInfoService.updateByStudentId(studentId, info);
//            }
//        }
//    }

//@Service
//@RequiredArgsConstructor
//public class CheckupResultServiceImpl implements CheckupResultService {
//    private final CheckupResultRepository checkupResultRepository;
//    private final CheckupEventRepository checkupEventRepository;
//    private final CheckupCategoryRepository checkupCategoryRepository;
//    private final StudentRepository studentRepository;
//    private final CheckupConsentRepository checkupConsentRepository;
//    private final CheckupBasicInfoService checkupBasicInfoService;
//
//    @Override
//    public boolean isApproved(Long eventId, Integer studentId, Long categoryId) {
//        return checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId)
//                .stream()
//                .anyMatch(c -> c.getCategory().getId().equals(categoryId)
//                        && c.getConsentStatus() != null
//                        && c.getConsentStatus() == ConsentStatus.APPROVE);
//    }
//
//    @Override
//    public void saveResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt) {
//        CheckupResult result = CheckupResult.builder()
//                .event(checkupEventRepository.findById(eventId).orElseThrow())
//                .student(studentRepository.findById(studentId).orElseThrow())
//                .category(checkupCategoryRepository.findById(categoryId).orElseThrow())
//                .resultData(resultData)
//                .checkedAt(LocalDateTime.now())
//                .build();
//        checkupResultRepository.save(result);
//    }
//
//    @Override
//    public List<CheckupResult> getResultsForStudentInEvent(Long eventId, Integer studentId) {
//        return checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
//    }
//
//    @Override
//    public void updateResult(Long eventId, Integer studentId, Long categoryId, String resultData) {
//        List<CheckupResult> results = checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
//        CheckupResult result = results.stream()
//                .filter(r -> r.getCategory().getId().equals(categoryId))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Checkup result not found"));
//
//        result.setResultData(resultData);
//        result.setCheckedAt(LocalDateTime.now());
//        checkupResultRepository.save(result);
//    }
//
//    @Override
//    public void updateResultById(Long resultId, String resultData) {
//        CheckupResult result = checkupResultRepository.findById(resultId)
//                .orElseThrow(() -> new RuntimeException("Checkup result not found with ID: " + resultId));
//
//        result.setResultData(resultData);
//        result.setCheckedAt(LocalDateTime.now());
//        checkupResultRepository.save(result);
//
//        // Đồng thời cập nhật CheckupBasicInfo nếu là category cơ bản
//        Long categoryId = result.getCategory().getId();
//        Integer studentId = result.getStudent().getStudentId();
//        if (categoryId != null && studentId != null) {
//            CheckupBasicInfo info = checkupBasicInfoService.getByStudentId(studentId);
//            if (info == null) {
//                info = CheckupBasicInfo.builder().build();
//            }
//            boolean updated = false;
//            switch (categoryId.intValue()) {
//                case 1:
//                    try { info.setHeight(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 2:
//                    try { info.setWeight(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 3:
//                    info.setBloodType(resultData); updated = true;
//                    break;
//                case 4:
//                    try { info.setVisionLeft(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 5:
//                    try { info.setVisionRight(resultData); updated = true; } catch (Exception ignored) {}
//                    break;
//                case 6:
//                    info.setUnderlyingDiseases(resultData); updated = true;
//                    break;
//                case 7:
//                    info.setAllergies(resultData); updated = true;
//                    break;
//                default:
//                    break;
//            }
//            if (updated) {
//                checkupBasicInfoService.updateByStudentId(studentId, info);
//            }
//        }
//    }
//
//    @Override
//    public void upsertResult(Long eventId, Integer studentId, Long categoryId, String resultData, String checkedAt) {
//        CheckupResult result = checkupResultRepository.findByEvent_IdAndStudent_StudentIdAndCategory_Id(eventId, studentId, categoryId);
//        java.time.LocalDateTime checkedAtTime = checkedAt != null ? java.time.LocalDateTime.parse(checkedAt) : java.time.LocalDateTime.now();
//        if (result != null) {
//            result.setResultData(resultData);
//            result.setCheckedAt(checkedAtTime);
//            checkupResultRepository.save(result);
//        } else {
//            result = CheckupResult.builder()
//                    .event(checkupEventRepository.findById(eventId).orElseThrow())
//                    .student(studentRepository.findById(studentId).orElseThrow())
//                    .category(checkupCategoryRepository.findById(categoryId).orElseThrow())
//                    .resultData(resultData)
//                    .checkedAt(checkedAtTime)
//                    .build();
//            checkupResultRepository.save(result);
//        }
//    }
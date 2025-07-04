package com.medischool.backend.service.impl;

import com.medischool.backend.dto.medication.MedicationDispensationDTO;
import com.medischool.backend.dto.medication.MedicationRequestDTO;
import com.medischool.backend.dto.medication.MedicationRequestItemDTO;
import com.medischool.backend.dto.medication.MedicationStatsDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;
import com.medischool.backend.model.medication.MedicationRequestItem;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.medication.MedicationDispensationRepository;
import com.medischool.backend.repository.medication.MedicationRequestItemRepository;
import com.medischool.backend.repository.medication.MedicationRequestRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MedicationServiceImpl implements MedicationService {
    @Autowired
    private MedicationRequestRepository requestRepo;
    @Autowired
    private MedicationRequestItemRepository requestItemRepo;
    @Autowired
    private MedicationDispensationRepository dispensationRepo;
    @Autowired
    private ParentStudentLinkRepository parentStudentRepo;
    @Autowired
    private UserProfileRepository userRepo;

    //Xem đơn dặn thuốc của học sinh
    @Override
    public List<MedicationRequest> getRequestsByStudent(Integer studentId, UUID parentId) throws AccessDeniedException {
        boolean linked = parentStudentRepo.existsByParentIdAndStudentId(parentId, studentId);
        if (!linked) {
            throw new AccessDeniedException("Đã có lỗi xảy ra, vui lòng thử lại.");
        }
        return requestRepo.findByStudentStudentId(studentId);
    }

    //Phụ huynh tạo đơn dặn thuốc
    @Override
    public MedicationRequest createRequest(MedicationRequestDTO dto, UUID parentId) throws AccessDeniedException {
        MedicationRequest request = requestRepo.findById(dto.getRequestId()).get();
        request.setMedicationStatus(MedicationStatus.PENDING);
        request.setCreateAt(OffsetDateTime.now());
        request.setParent(request.getParent());
        return requestRepo.save(request);
    }

    //Y tá chấp thuận đơn
    @Override
    public MedicationRequest approveRequest(int id, UUID nurseId) {
        MedicationRequest request = requestRepo.findById(id).orElseThrow();
        request.setMedicationStatus(MedicationStatus.APPROVED);
        request.setConfirmBy(nurseId);
        return requestRepo.save(request);
    }

    //Y tá từ chối đơn
    @Override
    public MedicationRequest rejectRequest(int id, UUID nurseId, String reason) {
        MedicationRequest request = requestRepo.findById(id).orElseThrow();
        request.setMedicationStatus(MedicationStatus.REJECTED);
        request.setRejectReason(reason);
        return requestRepo.save(request);
    }

    @Override
    public MedicationRequest resubmitRequest(Integer requestId, MedicationRequest updated, UUID parentId) {
        MedicationRequest existing = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("ID không tồn tại, vui lòng thử lại!"));

        if (!existing.getMedicationStatus().equals(MedicationStatus.REJECTED)) {
            throw new IllegalStateException("Bạn không thể chỉnh sửa đơn dặn thuốc này. Hãy liên hệ y tá nếu cần hỗ trợ.");
        }

        // Update fields
        existing.setTitle(updated.getTitle());
        existing.setNote(updated.getNote());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        // Reset status and review fields
        existing.setMedicationStatus(MedicationStatus.PENDING);
        existing.setRejectReason(null);
        existing.setUpdateAt(OffsetDateTime.now());

        // Xử lý items nếu cần (update hoặc thay thế toàn bộ)
        if (updated.getItems() != null) {
            existing.getItems().clear();
            for (MedicationRequestItem item : updated.getItems()) {
                item.setRequest(existing);
                existing.getItems().add(item);
            }
        }

        return requestRepo.save(existing);
    }

    //Phát thuốc
    @Override
    public MedicationDispensation dispenseMedication(MedicationDispensationDTO dto, UUID nurseId) {
        MedicationDispensation dispensation = new MedicationDispensation();
        dispensation.setDosageGiven(dto.getDose());
        dispensation.setNote(dto.getNote());
        dispensation.setStatus(dto.getStatus());
        dispensation.setTime(OffsetDateTime.now());
        dispensation.setNurseId(nurseId);
        MedicationRequest request = requestRepo.findById(dto.getRequestId()).get();
        request.setUpdateAt(OffsetDateTime.now());

        if (dto.getItemId() != null) {
            MedicationRequestItem item = requestItemRepo.findById(dto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy item"));

            dispensation.setItem(item);
            dispensation.setRequest(item.getRequest());
        } else if (dto.getRequestId() != null) {
            request = requestRepo.findById(dto.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc"));

            dispensation.setRequest(request);
        } else {
            throw new IllegalArgumentException("Phải cung cấp ít nhất itemId hoặc requestId.");
        }

        return dispensationRepo.save(dispensation);
    }


    //Xác nhận đơn thuốc đã hoàn thành
    @Override
    public MedicationRequest markAsDone(Integer requestId) {
        MedicationRequest request = requestRepo.findById(requestId).orElseThrow();
        request.setMedicationStatus(MedicationStatus.DONE);
        return requestRepo.save(request);
    }

    @Override
    public List<MedicationRequest> getAllRequests() {
        return requestRepo.findAll();
    }

    @Override
    public List<MedicationRequest> getRequestsByStatus(MedicationStatus status) {
        return requestRepo.findByMedicationStatus(status);
    }

    @Override
    public List<MedicationRequest> searchRequests(String keyword) {
        return requestRepo.searchByKeyword(keyword);
    }

    @Override
    public MedicationStatsDTO getRequestStats() {
        long total = requestRepo.count();
        long pending = requestRepo.countByMedicationStatus(MedicationStatus.PENDING);
        long approved = requestRepo.countByMedicationStatus(MedicationStatus.APPROVED);
        long rejected = requestRepo.countByMedicationStatus(MedicationStatus.REJECTED);
        return new MedicationStatsDTO(total, pending, approved, rejected);
    }

    @Override
    public List<MedicationDispensation> getDispensationsByRequestId(Integer requestId) {
        return dispensationRepo.findByRequestRequestId(requestId);
    }

    @Override
    public MedicationRequestDTO getRequestDetail(Integer requestId) {
        MedicationRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found for id: " + requestId));

        Student student = request.getStudent();
        UserProfile parent = request.getParent();
        UserProfile nurse = userRepo.findSingleById(request.getReviewBy());
        UserProfile manager = userRepo.findSingleById(request.getConfirmBy());
        List<MedicationRequestItem> items = request.getItems();
        List<MedicationDispensation> allDispensations = getDispensationsByRequestId(requestId);

        List<MedicationRequestItemDTO> itemDTOs = items.stream().map(item ->
                MedicationRequestItemDTO.builder()
                        .medicineName(item.getMedicineName())
                        .quantity(item.getQuantity())
                        .unit(item.getUnit())
                        .dosage(item.getDosage())
                        .note(item.getNote())
                        .build()
        ).toList();

        List<MedicationDispensationDTO> dispensationDTOs = allDispensations.stream().map(d -> {
            UserProfile nursedispen = userRepo.findSingleById(d.getNurseId());
            return MedicationDispensationDTO.builder()
                    .nurseName(nursedispen != null ? nursedispen.getFullName() : "Không rõ")
                    .dose(d.getDosageGiven())
                    .note(d.getNote())
                    .status(d.getStatus())
                    .time(d.getTime())
                    .build();
        }).toList();

        return MedicationRequestDTO.builder()
                .requestId(request.getRequestId())
                .nurseName(nurse != null ? nurse.getFullName() : "Không rõ")
                .managerName(manager != null ? manager.getFullName() : "Không rõ")
                .title(request.getTitle())
                .reason(request.getReason())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .createAt(request.getCreateAt())
                .updateAt(request.getUpdateAt())
                .medicationStatus(request.getMedicationStatus().name())
                .parent(parent)
                .student(student)
                .items(itemDTOs)
                .dispensations(dispensationDTOs)
                .build();
    }

}

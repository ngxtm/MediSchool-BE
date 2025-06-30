package com.medischool.backend.service.impl;

//import com.medischool.backend.dto.medication.MedicationStatsDTO;
import com.medischool.backend.model.enums.MedicationStatus;
import com.medischool.backend.model.medication.MedicationDispensation;
import com.medischool.backend.model.medication.MedicationRequest;
import com.medischool.backend.model.medication.MedicationRequestItem;
import com.medischool.backend.repository.medication.MedicationDispensationRepository;
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
    private MedicationDispensationRepository dispensationRepo;
    @Autowired
    private ParentStudentLinkRepository parentStudentRepo;

    //Xem đơn dặn thuốc của học sinh
    public List<MedicationRequest> getRequestsByStudent(Integer studentId, UUID parentId) throws AccessDeniedException {
        boolean linked = parentStudentRepo.existsByParentIdAndStudentId(parentId, studentId);
        if (!linked) {
            throw new AccessDeniedException("Đã có lỗi xảy ra, vui lòng thử lại.");
        }
        return requestRepo.findByStudentStudentId(studentId);
    }

    //Phụ huynh tạo đơn dặn thuốc
    public MedicationRequest createRequest(MedicationRequest request) {
        request.setMedicationStatus(MedicationStatus.PENDING);
        request.setCreateAt(OffsetDateTime.now());
        return requestRepo.save(request);
    }

    //Y tá chấp thuận đơn
    public MedicationRequest approveRequest(int id, UUID nurseId) {
        MedicationRequest req = requestRepo.findById(id).orElseThrow();
        req.setMedicationStatus(MedicationStatus.APPROVED);
        req.setConfirmBy(nurseId);
        return requestRepo.save(req);
    }

    //Y tá từ chối đơn
    public MedicationRequest rejectRequest(int id, UUID nurseId, String reason) {
        MedicationRequest req = requestRepo.findById(id).orElseThrow();
        req.setMedicationStatus(MedicationStatus.REJECTED);
        req.setRejectReason(reason);
        return requestRepo.save(req);
    }

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
    public MedicationDispensation dispenseMedication(int requestId, MedicationDispensation disp) {
        MedicationRequest request = requestRepo.findById(requestId).orElseThrow();
        disp.setRequest(request);
        disp.setTime(OffsetDateTime.now());
        return dispensationRepo.save(disp);
    }

    //Xác nhận đơn thuốc đã hoàn thành
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
        return requestRepo.findAll();
    }

//    @Override
//    public MedicationStatsDTO getRequestStats() {
//        long total = requestRepo.count();
//        long approved = requestRepo.countByMedicationStatus(MedicationStatus.APPROVED);
//        long rejected = requestRepo.countByMedicationStatus(MedicationStatus.REJECTED);
//        return new MedicationStatsDTO(total, approved, rejected);
//    }
}

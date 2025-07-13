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
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.medication.MedicationDispensationRepository;
import com.medischool.backend.repository.medication.MedicationRequestItemRepository;
import com.medischool.backend.repository.medication.MedicationRequestRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.service.MedicationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void resetMedicationRequestSequence() {
        Integer maxId = jdbcTemplate.queryForObject("SELECT MAX(request_id) FROM medication_request", Integer.class);
        int nextId = (maxId != null ? maxId + 1 : 1);
        jdbcTemplate.update("SELECT setval('medication_request_request_id_seq', ?, false)", nextId);
    }

    @Override
    public List<MedicationRequest> getRequestsByStudent(Integer studentId, UUID parentId) throws AccessDeniedException {
        boolean linked = parentStudentRepo.existsByParentIdAndStudentId(parentId, studentId);
        if (!linked) {
            throw new AccessDeniedException("Đã có lỗi xảy ra, vui lòng thử lại.");
        }
        return requestRepo.findByStudentStudentId(studentId);
    }

    @Override
    @Transactional
    public MedicationRequest createRequest(MedicationRequestDTO dto, UUID parentId) {
        MedicationRequest request = new MedicationRequest();
        request.setTitle(dto.getTitle());
        request.setReason(dto.getReason());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setNote(dto.getNote());
        request.setCreateAt(OffsetDateTime.now());
        request.setUpdateAt(OffsetDateTime.now());
        request.setMedicationStatus(MedicationStatus.PENDING);
        request.setParent(userRepo.findById(parentId).orElseThrow());
        request.setStudent(studentRepo.findById(dto.getStudentId()).orElseThrow());

        try {
            request = requestRepo.save(request);
        } catch (Exception e) {
            if (e.getMessage().contains("duplicate key value violates unique constraint")) {
                resetMedicationRequestSequence();
                request = requestRepo.save(request);
            } else {
                throw e;
            }
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            MedicationRequest finalRequest = request;
            List<MedicationRequestItem> items = dto.getItems().stream().map(itemDTO -> {
                MedicationRequestItem item = new MedicationRequestItem();
                item.setRequest(finalRequest);
                item.setMedicineName(itemDTO.getMedicineName());
                item.setUnit(itemDTO.getUnit());
                item.setQuantity(itemDTO.getQuantity());
                item.setDosage(itemDTO.getDosage());
                item.setNote(itemDTO.getNote());
                return item;
            }).toList();

            requestItemRepo.saveAll(items);
        }
        return request;
    }

    @Override
    public MedicationRequest approveRequest(int id, UUID userId, String role) {
        MedicationRequest request = requestRepo.findById(id).orElseThrow();
        request.setUpdateAt(OffsetDateTime.now());
        if(role.equalsIgnoreCase("role_nurse")){
            request.setMedicationStatus(MedicationStatus.REVIEWED);
            request.setReviewBy(userId);
        } else {
            request.setMedicationStatus(MedicationStatus.APPROVED);
            request.setReviewBy(userId);
            request.setConfirmBy(userId);
        }
        return requestRepo.save(request);
    }

    @Override
    public MedicationRequest rejectRequest(int id, UUID userId, String reason, String role) {
        MedicationRequest request = requestRepo.findById(id).orElseThrow();
        request.setMedicationStatus(MedicationStatus.REJECTED);
        request.setUpdateAt(OffsetDateTime.now());
        request.setRejectReason(reason);
        if(role.equalsIgnoreCase("nurse")){
            request.setReviewBy(userId);
        } else {
            request.setReviewBy(userId);
            request.setConfirmBy(userId);
        }
        return requestRepo.save(request);
    }

    @Override
    public MedicationRequest receiveMedicine(int id) {
        MedicationRequest request = requestRepo.findById(id).orElseThrow();
        request.setMedicationStatus(MedicationStatus.DISPENSING);
        request.setUpdateAt(OffsetDateTime.now());
        return requestRepo.save(request);
    }

    @Override
    public MedicationDispensation dispenseMedication(MedicationDispensationDTO dto, UUID nurseId) {
        MedicationDispensation dispensation = new MedicationDispensation();
        dispensation.setDosageGiven(dto.getDose());
        dispensation.setNote(dto.getNote());
        dispensation.setStatus(dto.getStatus());
        dispensation.setTime(OffsetDateTime.now());
        dispensation.setNurseId(nurseId);

        MedicationRequest request = requestRepo.findById(dto.getRequestId()).orElseThrow();

        if (dto.getItemId() != null) {
            MedicationRequestItem item = requestItemRepo.findById(dto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy item"));

            dispensation.setItem(item);
            dispensation.setRequest(item.getRequest());
        } else {
            dispensation.setRequest(request);
        }

        return dispensationRepo.save(dispensation);
    }

    @Override
    public MedicationRequest markAsDone(Integer requestId) {
        MedicationRequest request = requestRepo.findById(requestId).orElseThrow();
        request.setMedicationStatus(MedicationStatus.DONE);
        request.setUpdateAt(OffsetDateTime.now());
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
                .studentId(student != null ? student.getStudentId() : null)
                .nurseName(nurse != null ? nurse.getFullName() : "Không rõ")
                .managerName(manager != null ? manager.getFullName() : "Không rõ")
                .rejectReason(request.getRejectReason())
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

    @Override
    public MedicationRequest disableRequest(Integer requestId) {
        MedicationRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc với ID: " + requestId));

        if (request.getMedicationStatus() == MedicationStatus.APPROVED || request.getMedicationStatus() == MedicationStatus.DONE) {
            throw new IllegalStateException("Không thể xóa đơn đã được duyệt hoặc hoàn thành.");
        }

        request.setMedicationStatus(MedicationStatus.DISABLED);
        request.setUpdateAt(OffsetDateTime.now());

        return requestRepo.save(request);
    }

    @Override
    public MedicationRequestDTO updateRequest(Integer id, MedicationRequestDTO dto) {
        MedicationRequest request = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc với ID: " + id));

        if (request.getMedicationStatus() == MedicationStatus.APPROVED
                || request.getMedicationStatus() == MedicationStatus.DONE) {
            throw new IllegalStateException("Không thể chỉnh sửa đơn đã được duyệt hoặc hoàn thành.");
        }

        request.setTitle(dto.getTitle());
        request.setNote(dto.getNote());
        request.setReason(dto.getReason());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setUpdateAt(OffsetDateTime.now());

        List<MedicationRequestItem> currentItems = request.getItems();
        if (currentItems != null && !currentItems.isEmpty()) {
            requestItemRepo.deleteAll(currentItems);
            currentItems.clear();
        }

        for (MedicationRequestItemDTO itemDto : dto.getItems()) {
            MedicationRequestItem item = new MedicationRequestItem();
            item.setRequest(request);
            item.setMedicineName(itemDto.getMedicineName());
            item.setDosage(itemDto.getDosage());
            item.setQuantity(itemDto.getQuantity());
            item.setUnit(itemDto.getUnit());
            item.setNote(itemDto.getNote());
            currentItems.add(item);
        }

        return getRequestDetail(requestRepo.save(request).getRequestId());
    }
}
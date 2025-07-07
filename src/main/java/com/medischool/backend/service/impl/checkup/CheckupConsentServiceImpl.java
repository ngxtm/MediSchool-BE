package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.model.checkup.CheckupConsent;
import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.parentstudent.ParentStudentLink;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.checkup.CheckupConsentRepository;
import com.medischool.backend.repository.checkup.CheckupCategoryRepository;
import com.medischool.backend.repository.checkup.CheckupEventRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.checkup.CheckupResultRepository;
import com.medischool.backend.model.checkup.CheckupResult;
import com.medischool.backend.service.checkup.CheckupConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.medischool.backend.model.enums.CheckupEventScope;

@Service
@RequiredArgsConstructor
public class CheckupConsentServiceImpl implements CheckupConsentService {
    private final CheckupConsentRepository checkupConsentRepository;
    private final CheckupCategoryRepository checkupCategoryRepository;
    private final CheckupEventRepository checkupEventRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final UserProfileRepository userProfileRepository;
    private final CheckupResultRepository checkupResultRepository;

    @Override
    public List<CheckupConsent> getConsentsForStudentInEvent(Long eventId, Integer studentId) {
        return checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
    }

    @Override
    public CheckupConsent getConsentById(Long consentId) {
        return checkupConsentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consent not found with ID: " + consentId));
    }

    @Override
    @Transactional
    public void submitConsentById(Long consentId, String consentStatus, String note) {
        CheckupConsent consent = getConsentById(consentId);
        consent.setConsentStatus(ConsentStatus.valueOf(consentStatus));
        consent.setNote(note);
        consent.setFullyRejected(false);
        
        // Nếu consentStatus là APPROVE, tạo CheckupResult nếu chưa có
        if (ConsentStatus.valueOf(consentStatus) == ConsentStatus.APPROVE) {
            boolean hasResult = checkupResultRepository.findByEvent_IdAndStudent_StudentId(
                consent.getEvent().getId(), 
                consent.getStudent().getStudentId()
            ).stream().anyMatch(r -> r.getCategory().getId().equals(consent.getCategory().getId()));
            
            if (!hasResult) {
                CheckupResult result = CheckupResult.builder()
                    .event(consent.getEvent())
                    .student(consent.getStudent())
                    .category(consent.getCategory())
                    .resultData(null)
                    .checkedAt(null)
                    .build();
                checkupResultRepository.save(result);
            }
        }
        
        checkupConsentRepository.save(consent);
    }

    @Override
    @Transactional
    public void submitAllConsentsForStudent(Long eventId, Integer studentId, String consentStatus, String note) {
        List<CheckupConsent> allConsents = checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
        
        for (CheckupConsent c : allConsents) {
            c.setConsentStatus(ConsentStatus.valueOf(consentStatus));
            c.setNote(note);
            c.setFullyRejected(ConsentStatus.valueOf(consentStatus) == ConsentStatus.REJECT);
            
            // Nếu consentStatus là APPROVE, tạo CheckupResult nếu chưa có
            if (ConsentStatus.valueOf(consentStatus) == ConsentStatus.APPROVE) {
                boolean hasResult = checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId)
                    .stream().anyMatch(r -> r.getCategory().getId().equals(c.getCategory().getId()));
                
                if (!hasResult) {
                    CheckupResult result = CheckupResult.builder()
                        .event(c.getEvent())
                        .student(c.getStudent())
                        .category(c.getCategory())
                        .resultData(null)
                        .checkedAt(null)
                        .build();
                    checkupResultRepository.save(result);
                }
            }
        }
        
        checkupConsentRepository.saveAll(allConsents);
    }

    @Override
    @Transactional
    public void submitConsents(Long eventId, Integer studentId, List<ConsentRequest> consents, Boolean fullyRejected) {
        Optional<CheckupEvent> eventOpt = checkupEventRepository.findById(eventId);
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (eventOpt.isEmpty() || studentOpt.isEmpty()) throw new RuntimeException("Event or student not found");
        CheckupEvent event = eventOpt.get();
        Student student = studentOpt.get();
        List<CheckupConsent> existing = checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId);
        if (Boolean.TRUE.equals(fullyRejected)) {
            for (CheckupConsent consent : existing) {
                consent.setConsentStatus(ConsentStatus.REJECT);
                consent.setFullyRejected(true);
            }
            checkupConsentRepository.saveAll(existing);
        } else {
            for (ConsentRequest req : consents) {
                CheckupConsent consent = existing.stream()
                        .filter(c -> c.getCategory().getId().equals(req.categoryId))
                        .findFirst()
                        .orElseGet(() -> {
                            CheckupCategory cat = checkupCategoryRepository.findById(req.categoryId).orElseThrow();
                            CheckupConsent c = new CheckupConsent();
                            c.setEvent(event);
                            c.setStudent(student);
                            c.setCategory(cat);
                            return c;
                        });
                consent.setConsentStatus(ConsentStatus.valueOf(req.consentStatus));
                consent.setFullyRejected(false);
                checkupConsentRepository.save(consent);
                // Nếu consentStatus là APPROVE, tạo CheckupResult nếu chưa có
                if (ConsentStatus.valueOf(req.consentStatus) == ConsentStatus.APPROVE) {
                    boolean hasResult = checkupResultRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId)
                        .stream().anyMatch(r -> r.getCategory().getId().equals(req.categoryId));
                    if (!hasResult) {
                        CheckupResult result = CheckupResult.builder()
                            .event(event)
                            .student(student)
                            .category(checkupCategoryRepository.findById(req.categoryId).orElseThrow())
                            .resultData(null)
                            .checkedAt(null)
                            .build();
                        checkupResultRepository.save(result);
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void sendConsentToAllParents(Long eventId) {
        CheckupEvent event = checkupEventRepository.findById(eventId).orElseThrow();
        List<CheckupCategory> categories = checkupCategoryRepository.findAll();
        List<ParentStudentLink> links = parentStudentLinkRepository.findAll();
        for (ParentStudentLink link : links) {
            Integer studentId = link.getStudentId();
            UserProfile parent = userProfileRepository.findById(link.getParentId()).orElseThrow();
            boolean shouldSend = true;
            if (event.getScope() == CheckupEventScope.NEED_RECHECK) {
                // Kiểm tra xem học sinh có kết quả khám nào không (trong bất kỳ event nào)
                boolean hasAnyResult = checkupResultRepository.findByStudent_StudentId(studentId).size() > 0;
                
                // Kiểm tra xem học sinh có consent nào được approve trong các event trước đó không
                boolean hasApprovedConsent = checkupConsentRepository.findByStudent_StudentId(studentId)
                    .stream()
                    .anyMatch(c -> c.getConsentStatus() == ConsentStatus.APPROVE);
                
                // Chỉ gửi consent nếu học sinh chưa từng được khám hoặc chưa có consent nào được approve
                shouldSend = !hasAnyResult || !hasApprovedConsent;
            }
            if (!shouldSend) continue;
            for (CheckupCategory category : categories) {
                boolean exists = checkupConsentRepository.findByEvent_IdAndStudent_StudentId(eventId, studentId)
                    .stream().anyMatch(c -> c.getCategory().getId().equals(category.getId()));
                if (!exists) {
                    CheckupConsent consent = CheckupConsent.builder()
                        .event(event)
                        .student(studentRepository.findById(studentId).orElseThrow())
                        .parent(parent)
                        .category(category)
                        .consentStatus(null)
                        .fullyRejected(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                    checkupConsentRepository.save(consent);
                }
            }
        }
    }
} 
package com.medischool.backend.controller.checkup;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.annotation.LogActivity;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;
import com.medischool.backend.model.checkup.CheckupBasicInfo;
import com.medischool.backend.service.checkup.CheckupBasicInfoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkup-basic-info")
@RequiredArgsConstructor
public class CheckupBasicInfoController {
    private final CheckupBasicInfoService checkupBasicInfoService;

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Lấy thông tin cơ bản của học sinh")
    public ResponseEntity<CheckupBasicInfo> getByStudentId(@PathVariable Integer studentId) {
        CheckupBasicInfo info = checkupBasicInfoService.getByStudentId(studentId);
        if (info == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(info);
    }

    @PutMapping("/student/{studentId}")
    @Operation(summary = "Cập nhật thông tin cơ bản của học sinh")
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.STUDENT,
        description = "Cập nhật thông tin cơ bản kiểm tra sức khỏe của học sinh",
        entityIdParam = "studentId"
    )
    public ResponseEntity<CheckupBasicInfo> updateByStudentId(@PathVariable Integer studentId, @RequestBody CheckupBasicInfo info) {
        return ResponseEntity.ok(checkupBasicInfoService.updateByStudentId(studentId, info));
    }
} 
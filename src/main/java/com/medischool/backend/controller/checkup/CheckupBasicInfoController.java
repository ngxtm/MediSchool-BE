package com.medischool.backend.controller.checkup;

import com.medischool.backend.model.checkup.CheckupBasicInfo;
import com.medischool.backend.service.checkup.CheckupBasicInfoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CheckupBasicInfo> updateByStudentId(@PathVariable Integer studentId, @RequestBody CheckupBasicInfo info) {
        return ResponseEntity.ok(checkupBasicInfoService.updateByStudentId(studentId, info));
    }
} 
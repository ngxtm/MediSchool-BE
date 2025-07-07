package com.medischool.backend.service.checkup;

import com.medischool.backend.model.checkup.CheckupBasicInfo;

public interface CheckupBasicInfoService {
    CheckupBasicInfo getByStudentId(Integer studentId);
    CheckupBasicInfo updateByStudentId(Integer studentId, CheckupBasicInfo info);
} 
package com.medischool.backend.controller;

import com.medischool.backend.dto.request.HealthProfileRequest;
import com.medischool.backend.dto.response.HealthProfileResponse;
import com.medischool.backend.service.HealthProfileService;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-profile")
@RequiredArgsConstructor
public class HealthProfileController{
    private final HealthProfileService healthProfileService;

    @PostMapping
    public ResponseEntity<HealthProfileResponse> saveHealthProfile(@RequestBody HealthProfileRequest healthProfileRequest)throws CustomException {
        return ResponseEntity.ok().body(this.healthProfileService.saveHealthProfile(healthProfileRequest));
    }
}

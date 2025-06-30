package com.medischool.backend.controller;


import com.medischool.backend.dto.request.PeriodicCheckupRequest;
import com.medischool.backend.dto.response.PeriodicCheckupResponse;
import com.medischool.backend.repository.PeriodicCheckupRepository;
import com.medischool.backend.service.PeriodicCheckupService;
import com.medischool.backend.util.annotation.ApiMessage;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periodic_checkup")
@RequiredArgsConstructor
public class PeriodicCheckupController {
    private final PeriodicCheckupService periodicCheckupService;


    @PostMapping
    @ApiMessage("Tạo mới lịch khám")
    public ResponseEntity<PeriodicCheckupResponse> createPeriodic(@RequestBody PeriodicCheckupRequest periodicCheckupRequest) throws CustomException {
        return ResponseEntity.ok().body(this.periodicCheckupService.savePeriodicCheckup(periodicCheckupRequest));
    }


    @PutMapping
    @ApiMessage("Cập nhật lịch khám")
    public ResponseEntity<PeriodicCheckupResponse> updatePeriodic(@RequestBody PeriodicCheckupRequest periodicCheckupRequest) throws CustomException {
        return ResponseEntity.ok().body(this.periodicCheckupService.savePeriodicCheckup(periodicCheckupRequest));
    }


    @DeleteMapping("/{periodicId}")
    @ApiMessage("Xóa lịch khám")
    public ResponseEntity<Void> deletePeriodic(@PathVariable("periodicId") Long periodicId) throws CustomException {
        this.periodicCheckupService.deletePeriodicCheckup(periodicId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/find/{periodicId}")
    @ApiMessage("Xem chi tiết lịch khám")
    public ResponseEntity<PeriodicCheckupResponse>  getPeriodicCheckupById(@PathVariable("periodicId") Long periodicId) throws CustomException {
        return ResponseEntity.ok().body(this.periodicCheckupService.getPeriodicCheckupById(periodicId));
    }

    @GetMapping("/find/{periodicYear}")
    @ApiMessage("Tìm lịch khám theo năm")
    public ResponseEntity<List<PeriodicCheckupResponse>> getPeriodicCheckupByYear(@PathVariable String periodicYear) throws CustomException {
        return ResponseEntity.ok().body(this.periodicCheckupService.getPeriodicCheckupByYear(periodicYear));
    }

}

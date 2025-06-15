package com.medischool.backend.controller;


import com.medischool.backend.dto.request.CheckupItemRequest;
import com.medischool.backend.dto.response.CheckupConsentResponse;
import com.medischool.backend.service.CheckupConsentService;
import com.medischool.backend.util.annotation.ApiMessage;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkup_consent")
@RequiredArgsConstructor
public class CheckupConsentController {
    private final CheckupConsentService checkupConsentService;

    @PostMapping("/status/{consentId}")
    @ApiMessage("Thay đổi trạng thái đơn khám bệnh")
    public ResponseEntity<Void> changeStatus(@PathVariable Long consentId) throws CustomException {
        this.checkupConsentService.changeStatusOfConsent(consentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accepted_checkup_items")
    @ApiMessage("Chấp nhận danh sách khám")
    public ResponseEntity<CheckupConsentResponse> acceptCheckupItem(@RequestBody CheckupItemRequest checkupItemRequest) throws CustomException {
        return ResponseEntity.ok().body(this.checkupConsentService.acceptCheckupConsent(checkupItemRequest));
    }
}

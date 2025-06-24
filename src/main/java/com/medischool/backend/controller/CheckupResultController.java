package com.medischool.backend.controller;


import com.medischool.backend.dto.request.CheckupResultRequest;
import com.medischool.backend.dto.response.CheckupResultResponse;
import com.medischool.backend.service.CheckupResultService;
import com.medischool.backend.util.annotation.ApiMessage;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkup_result")
@RequiredArgsConstructor
public class CheckupResultController {
    private final CheckupResultService checkupResultService;

    @PostMapping
    @ApiMessage("Save checkup result")
    public ResponseEntity<CheckupResultResponse> saveCheckupResult(@RequestBody CheckupResultRequest checkupResultRequest) throws CustomException {
        return ResponseEntity.ok().body(this.checkupResultService.saveCheckupResult(checkupResultRequest));
    }

}

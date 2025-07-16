package com.medischool.backend.service.checkup;

public class SendConsentResult {
    public int success;
    public int failed;
    public SendConsentResult(int success, int failed) {
        this.success = success;
        this.failed = failed;
    }
} 
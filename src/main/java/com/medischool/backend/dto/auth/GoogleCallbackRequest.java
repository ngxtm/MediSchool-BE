package com.medischool.backend.dto.auth;

import lombok.Data;

import java.util.Map;

@Data
public class GoogleCallbackRequest {
    private Map<String, Object> supabaseSession;
    private boolean rememberMe;
}

package com.medischool.backend.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private boolean rememberMe;
}

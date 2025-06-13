package com.medischool.backend.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private boolean rememberMe;
}

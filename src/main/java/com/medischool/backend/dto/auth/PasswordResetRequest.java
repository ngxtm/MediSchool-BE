package com.medischool.backend.dto.auth;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String newPassword;
}

package com.medischool.backend.controller;

import java.util.Map;

import com.medischool.backend.model.LoginHistory;
import com.medischool.backend.service.GeolocationService;
import com.medischool.backend.service.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.auth.AuthRequest;
import com.medischool.backend.dto.auth.AuthResponse;
import com.medischool.backend.dto.auth.GoogleCallbackRequest;
import com.medischool.backend.dto.auth.PasswordResetRequest;
import com.medischool.backend.service.SupabaseAuthService;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @Autowired
    private LoginHistoryService loginHistoryService;

    @Autowired
    private GeolocationService geolocationService;

    @PostMapping("/signin")
    @Operation(summary = "Sign in with email and password")
    @com.medischool.backend.annotation.LogActivity(
        actionType = com.medischool.backend.model.ActivityLog.ActivityType.LOGIN,
        entityType = com.medischool.backend.model.ActivityLog.EntityType.USER,
        description = "Đăng nhập hệ thống"
    )
    public ResponseEntity<AuthResponse> signIn(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        AuthResponse authResponse = supabaseAuthService.signInWithEmail(
                request.getEmail(),
                request.getPassword(),
                request.isRememberMe());
        try {
            String ip = getClientIpAddress(httpRequest);
            String location = geolocationService.getLocationFromIp(ip);

            if (authResponse.getUser() != null && authResponse.getToken() != null) {
                loginHistoryService.createLoginRecord(
                        authResponse.getUser().getId(),
                        authResponse.getUser().getEmail(),
                        ip,
                        httpRequest.getHeader("User-Agent"),
                        location,
                        LoginHistory.LoginStatus.SUCCESS,
                        null
                );
            } else {
                loginHistoryService.createLoginRecord(
                        null,
                        request.getEmail(),
                        ip,
                        httpRequest.getHeader("User-Agent"),
                        location,
                        LoginHistory.LoginStatus.FAILED,
                        "Authentication failed"
                );
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                    .error("Failed to record login history: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up with email and password")
    @com.medischool.backend.annotation.LogActivity(
        actionType = com.medischool.backend.model.ActivityLog.ActivityType.CREATE,
        entityType = com.medischool.backend.model.ActivityLog.EntityType.USER,
        description = "Tạo tài khoản người dùng mới"
    )
    public ResponseEntity<AuthResponse> signUp(@RequestBody AuthRequest request) {
        AuthResponse authResponse = supabaseAuthService.signUpWithEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/google-callback")
    @Operation(summary = "Handle Google Auth callback")
    public ResponseEntity<AuthResponse> googleCallback(@RequestBody GoogleCallbackRequest request) {
        AuthResponse response = supabaseAuthService.handleGoogleCallback(request.getSupabaseSession(),
                request.isRememberMe());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Request password reste")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody PasswordResetRequest request) {
        supabaseAuthService.resetPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-password")
    @Operation(summary = "Update password")
    public ResponseEntity<AuthResponse> updatePassword(@RequestBody PasswordResetRequest request) {
        supabaseAuthService.updatePassword(request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signout")
    @Operation(summary = "Sign out")
    @com.medischool.backend.annotation.LogActivity(
        actionType = com.medischool.backend.model.ActivityLog.ActivityType.LOGOUT,
        entityType = com.medischool.backend.model.ActivityLog.EntityType.USER,
        description = "Đăng xuất hệ thống"
    )
    public ResponseEntity<AuthResponse> signOut(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            supabaseAuthService.signOut(token);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh authentication token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No valid authorization header provided"));
            }

            String token = authHeader.substring("Bearer ".length());
            boolean isSupabaseToken = token.contains(".");
            try {
                AuthResponse authResponse;
                if (token.contains(".")) {
                    authResponse = supabaseAuthService.refreshSupabaseToken(token);
                } else {
                    authResponse = supabaseAuthService.refreshToken(token);
                }
                return ResponseEntity.ok(authResponse);
            } catch (ExpiredJwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token has expired"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

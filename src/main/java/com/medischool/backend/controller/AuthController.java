package com.medischool.backend.controller;

import com.medischool.backend.dto.AuthRequest;
import com.medischool.backend.dto.AuthResponse;
import com.medischool.backend.dto.GoogleCallbackRequest;
import com.medischool.backend.dto.PasswordResetRequest;
import com.medischool.backend.service.SupabaseAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @PostMapping("/signin")
    @Operation(summary = "Sign in with email and password")
    public ResponseEntity<AuthResponse> signIn(@RequestBody AuthRequest request) {
        AuthResponse authResponse = supabaseAuthService.signInWithEmail(
                request.getEmail(),
                request.getPassword(),
                request.isRememberMe()
        );
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up with email and password")
    public ResponseEntity<AuthResponse> signUp(@RequestBody AuthRequest request) {
        AuthResponse authResponse = supabaseAuthService.signUpWithEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/google-callback")
    @Operation(summary = "Handle Google Auth callback")
    public ResponseEntity<AuthResponse> googleCallback(@RequestBody GoogleCallbackRequest  request) {
        AuthResponse response = supabaseAuthService.handleGoogleCallback(request.getSupabaseSession(), request.isRememberMe());
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

            AuthResponse authResponse = supabaseAuthService.refreshToken(token);

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Failed to refresh token: " + e.getMessage()));
        }
    }
}

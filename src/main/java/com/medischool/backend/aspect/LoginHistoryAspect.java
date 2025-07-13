package com.medischool.backend.aspect;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.medischool.backend.dto.auth.AuthRequest;
import com.medischool.backend.dto.auth.AuthResponse;
import com.medischool.backend.model.LoginHistory;
import com.medischool.backend.model.LoginHistory.LoginStatus;
import com.medischool.backend.service.GeolocationService;
import com.medischool.backend.service.LoginHistoryService;
import com.medischool.backend.service.SupabaseAuthService;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoginHistoryAspect {
    
    @Autowired
    private LoginHistoryService loginHistoryService;
    
    @Autowired
    private GeolocationService geolocationService;
    
    @Autowired
    private SupabaseAuthService supabaseAuthService;
    
    private final javax.crypto.SecretKey secretKey;
    
    public LoginHistoryAspect(@org.springframework.beans.factory.annotation.Value("${supabase.jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    @AfterReturning(
        pointcut = "execution(* com.medischool.backend.controller.AuthController.signIn(..))",
        returning = "result"
    )
    public void logLoginActivity(JoinPoint joinPoint, Object result) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (result instanceof org.springframework.http.ResponseEntity) {
                Object body = ((org.springframework.http.ResponseEntity<?>) result).getBody();
                if (body instanceof AuthResponse) {
                    AuthResponse authResponse = (AuthResponse) body;
                    Object[] args = joinPoint.getArgs();
                    String email = "unknown";
                    if (args.length > 0 && args[0] instanceof AuthRequest) {
                        AuthRequest authRequest = (AuthRequest) args[0];
                        email = authRequest.getEmail();
                    }
                    if (authResponse.getUser() != null && authResponse.getToken() != null) {
                        UUID userId = null;
                        String userEmail = email;
                        if (authResponse.getUser().getId() != null) {
                            userId = authResponse.getUser().getId();
                        }
                        if (authResponse.getUser().getEmail() != null) {
                            userEmail = authResponse.getUser().getEmail();
                        }
                        String ip = getClientIpAddress(request);
                        String location = geolocationService.getLocationFromIp(ip);
                        loginHistoryService.createLoginRecord(
                            userId,
                            userEmail,
                            ip,
                            request.getHeader("User-Agent"),
                            location,
                            LoginStatus.SUCCESS,
                            null
                        );
                    } else {
                        String ip = getClientIpAddress(request);
                        String location = geolocationService.getLocationFromIp(ip);
                        loginHistoryService.createLoginRecord(
                            null,
                            email,
                            ip,
                            request.getHeader("User-Agent"),
                            location,
                            LoginStatus.FAILED,
                            "Authentication failed"
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in LoginHistoryAspect: {}", e.getMessage(), e);
        }
    }
    
    @AfterReturning(
        pointcut = "execution(* com.medischool.backend.controller.AuthController.signOut(..))"
    )
    public void logLogoutActivity(JoinPoint joinPoint) {
        log.info("=== LoginHistoryAspect triggered for signOut method ===");
        
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring("Bearer ".length());
                
                UserInfo userInfo = extractUserInfoFromToken(token);
                
                if (userInfo.getUserId() != null) {
                    java.util.List<LoginHistory> activeSessions = loginHistoryService.getActiveSessionsRaw().stream()
                        .filter(lh -> userInfo.getUserId().equals(lh.getUserId()))
                        .toList();
                    if (!activeSessions.isEmpty()) {
                        LoginHistory active = activeSessions.get(0);
                        loginHistoryService.updateLogoutTime(active.getId());
                        log.info("Updated logout time for session ID: {}", active.getId());
                    }
                }
                
                String ip = getClientIpAddress(request);
                String location = geolocationService.getLocationFromIp(ip);
                loginHistoryService.createLoginRecord(
                    userInfo.getUserId(),
                    userInfo.getUserEmail(),
                    ip,
                    request.getHeader("User-Agent"),
                    location,
                    LoginStatus.SUCCESS,
                    null
                );
                log.info("Logout record created for user: {}", userInfo.getUserEmail());
            }
        } catch (Exception e) {
            log.error("Error in LoginHistoryAspect for logout: {}", e.getMessage(), e);
        }
    }
    
    private UserInfo extractUserInfoFromToken(String token) {
        try {
            Map<String, Object> userInfo = supabaseAuthService.extractUserInfoFromToken(token);
            if (userInfo != null) {
                String userIdStr = (String) userInfo.get("id");
                String userEmail = (String) userInfo.get("email");
                
                UUID userId = null;
                if (userIdStr != null) {
                    userId = UUID.fromString(userIdStr);
                }
                if (userEmail == null) {
                    userEmail = "unknown";
                }
                
                return new UserInfo(userId, userEmail);
            }
        } catch (Exception e) {
            log.warn("Could not extract user info from token: {}", e.getMessage());
        }
        return new UserInfo(null, "unknown");
    }
    
    private static class UserInfo {
        private final UUID userId;
        private final String userEmail;
        
        public UserInfo(UUID userId, String userEmail) {
            this.userId = userId;
            this.userEmail = userEmail;
        }
        
        public UUID getUserId() { return userId; }
        public String getUserEmail() { return userEmail; }
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
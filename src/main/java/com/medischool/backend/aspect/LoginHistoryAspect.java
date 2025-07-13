package com.medischool.backend.aspect;

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
    
    @AfterReturning(
        pointcut = "@annotation(com.medischool.backend.annotation.LogActivity) && " +
                  "execution(* com.medischool.backend.controller.AuthController.signIn(..))",
        returning = "result"
    )
    public void logLoginActivity(JoinPoint joinPoint, Object result) {
        log.info("LoginHistoryAspect triggered for signIn method");
        
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            
            AuthResponse authResponse = null;
            if (result instanceof AuthResponse) {
                authResponse = (AuthResponse) result;
            } else if (result instanceof org.springframework.http.ResponseEntity) {
                Object body = ((org.springframework.http.ResponseEntity<?>) result).getBody();
                if (body instanceof AuthResponse) {
                    authResponse = (AuthResponse) body;
                }
            }
            if (authResponse != null) {
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
                    log.info("Login attempt from IP: {}", ip);
                    String location = geolocationService.getLocationFromIp(ip);
                    LoginHistory loginHistory = loginHistoryService.createLoginRecord(
                        userId,
                        userEmail,
                        ip,
                        request.getHeader("User-Agent"),
                        location,
                        LoginStatus.SUCCESS,
                        null
                    );
                    log.info("Login record created successfully with ID: {}", loginHistory.getId());
                } else {
                    String ip = getClientIpAddress(request);
                    log.info("Failed login attempt from IP: {}", ip);
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
        } catch (Exception e) {
            log.error("Error in LoginHistoryAspect: {}", e.getMessage(), e);
        }
    }
    
    @AfterReturning(
        pointcut = "@annotation(com.medischool.backend.annotation.LogActivity) && " +
                  "execution(* com.medischool.backend.controller.AuthController.signOut(..))"
    )
    public void logLogoutActivity(JoinPoint joinPoint) {
        log.info("LoginHistoryAspect triggered for signOut method");
        
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                UUID userId = null;
                String userEmail = "unknown";
                if (userId != null) {
                    java.util.List<LoginHistory> activeSessions = loginHistoryService.getActiveSessionsRaw().stream()
                        .filter(lh -> userId.equals(lh.getUserId()))
                        .toList();
                    if (!activeSessions.isEmpty()) {
                        LoginHistory active = activeSessions.get(0);
                        loginHistoryService.updateLogoutTime(active.getId());
                    }
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
            }
        } catch (Exception e) {
            log.error("Error in LoginHistoryAspect for logout: {}", e.getMessage(), e);
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
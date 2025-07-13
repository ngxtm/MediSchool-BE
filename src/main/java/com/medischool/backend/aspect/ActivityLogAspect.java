package com.medischool.backend.aspect;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.medischool.backend.annotation.LogActivity;
import com.medischool.backend.service.ActivityLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogAspect {
    
    private final ActivityLogService activityLogService;
    
    @Around("@annotation(com.medischool.backend.annotation.LogActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogActivity logActivity = method.getAnnotation(LogActivity.class);
        
        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (Exception e) {
            log.warn("Could not get request attributes: {}", e.getMessage());
        }
        
        UUID userId = null;
        String userName = "Unknown";
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                userId = UUID.fromString(authentication.getName());
                userName = authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Could not get user information: {}", e.getMessage());
        }
        
        String entityId = "";
        if (!logActivity.entityIdParam().isEmpty()) {
            try {
                String[] paramNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < paramNames.length; i++) {
                    if (logActivity.entityIdParam().equals(paramNames[i])) {
                        entityId = args[i] != null ? args[i].toString() : "";
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("Could not get entity ID from parameter: {}", e.getMessage());
            }
        }
        
        String ipAddress = "";
        String userAgent = "";
        if (request != null) {
            ipAddress = getClientIpAddress(request);
            userAgent = request.getHeader("User-Agent");
        }
        
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            String parsedDescription = parseTemplate(logActivity.description(), signature.getParameterNames(), joinPoint.getArgs(), result);
            activityLogService.createActivityLog(
                userId,
                userName,
                logActivity.actionType(),
                logActivity.entityType(),
                entityId,
                parsedDescription,
                "Success",
                ipAddress,
                userAgent
            );
            
        } catch (Exception e) {
            String parsedDescription = parseTemplate(logActivity.description(), signature.getParameterNames(), joinPoint.getArgs(), null) + " (Failed: " + e.getMessage() + ")";
            activityLogService.createActivityLog(
                userId,
                userName,
                logActivity.actionType(),
                logActivity.entityType(),
                entityId,
                parsedDescription,
                "Error: " + e.getMessage(),
                ipAddress,
                userAgent
            );
            throw e;
        }
        
        return result;
    }
    
    private String parseTemplate(String template, String[] paramNames, Object[] args, Object result) {
        String parsed = template;
        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] != null) {
                    parsed = parsed.replace("{" + paramNames[i] + "}", args[i].toString());
                }
            }
        }
        if (result != null) {
            if (result instanceof java.util.Map) {
                java.util.Map<?,?> map = (java.util.Map<?,?>) result;
                for (Object key : map.keySet()) {
                    Object val = map.get(key);
                    if (val != null)
                        parsed = parsed.replace("{" + key + "}", val.toString());
                }
            }
            else {
                java.beans.BeanInfo info;
                try {
                    info = java.beans.Introspector.getBeanInfo(result.getClass());
                    for (java.beans.PropertyDescriptor pd : info.getPropertyDescriptors()) {
                        String name = pd.getName();
                        if ("class".equals(name)) continue;
                        Object val = pd.getReadMethod().invoke(result);
                        if (val != null)
                            parsed = parsed.replace("{" + name + "}", val.toString());
                    }
                } catch (Exception ignore) {}
            }
        }
        return parsed;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 
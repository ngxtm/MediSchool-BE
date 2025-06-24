package com.medischool.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medischool.backend.dto.auth.AuthResponse;
import com.medischool.backend.dto.UserDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.UserProfileRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SupabaseAuthService {
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api.key}")
    private String supabaseApiKey;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    @Value("${app.jwt.expiration.minutes}")
    private long jwtExpirationMinutes;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final SecretKey secretKey;
    private final Set<String> tokenBlacklist = new HashSet<>();

    public SupabaseAuthService(@Value("${supabase.jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthResponse signInWithEmail(String email, String password, boolean rememberMe) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/token?grant_type=password",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Authentication failed: Empty response from Supabase");
        }

        return processAuthResponse(responseBody, rememberMe);
    }

    public AuthResponse signUpWithEmail(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/signup",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Signup failed: Empty response from Supabase");
        }

        return processAuthResponse(responseBody, true);
    }

    public AuthResponse handleGoogleCallback(Map<String, Object> supabaseSession, boolean rememberMe) {
        String accessToken = (String) ((Map<?, ?>) supabaseSession).get("access_token");
        if (accessToken == null) {
            throw new RuntimeException("Invalid Supabase session: Missing access token");
        }

        Map<String, Object> userInfo = extractUserInfoFromToken(accessToken);

        UserProfile userProfile = syncUserProfile(userInfo);

        String customToken = generateJwtToken(userProfile.getId().toString(), userProfile.getEmail());

        UserDTO userDto = UserDTO.builder()
                .id(userProfile.getId())
                .email(userProfile.getEmail())
                .fullName(userProfile.getFullName())
                .role(userProfile.getRole())
                .build();

        return AuthResponse.builder()
                .token(customToken)
                .session(supabaseSession)
                .user(userDto)
                .build();
    }

    public void resetPassword(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("redirectTo", frontEndUrl + "/update-password#recovery=true");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        restTemplate.exchange(
                supabaseUrl + "/auth/v1/recover",
                HttpMethod.POST,
                request,
                Void.class
        );
    }

    public void updatePassword(String newPassword) {
        String userToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseApiKey);
        headers.set("Authorization", "Bearer " + userToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("password", newPassword);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        restTemplate.exchange(
                supabaseUrl + "/auth/v1/user",
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public AuthResponse refreshToken(String token) {
        if (tokenBlacklist.contains(token)) {
            throw new RuntimeException("Token has been invalidated");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);

            String newToken = generateJwtToken(userId, email);

            Optional<UserProfile> userProfileOpt = userProfileRepository.findById(UUID.fromString(userId));
            if (userProfileOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            UserProfile userProfile = userProfileOpt.get();
            UserDTO userDto = UserDTO.builder()
                    .id(userProfile.getId())
                    .email(userProfile.getEmail())
                    .fullName(userProfile.getFullName())
                    .role(userProfile.getRole())
                    .build();

            return AuthResponse.builder()
                    .token(newToken)
                    .user(userDto)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    public AuthResponse refreshSupabaseToken(String token) {
        Map<String, Object> userInfo = extractUserInfoFromToken(token);

        if (userInfo == null) {
            throw new RuntimeException("Failed to extract user info token");
        }

        String userId = userInfo.get("id").toString();
        String email = userInfo.get("email").toString();

        UserProfile userProfile = syncUserProfile(userInfo);

        String customToken = generateJwtToken(userId, email);
        UserDTO userDTO = UserDTO.builder()
                .id(userProfile.getId())
                .email(userProfile.getEmail())
                .fullName(userProfile.getFullName())
                .role(userProfile.getRole())
                .build();

        return AuthResponse.builder()
                .token(customToken)
                .session(userInfo)
                .user(userDTO)
                .build();
    }

    public void signOut(String token) {
        tokenBlacklist.add(token);
    }

    private AuthResponse processAuthResponse(Map<String, Object> supabaseResponse, boolean rememberMe) {
        try {
            Map<String, Object> userData = (Map<String, Object>) supabaseResponse.get("user");
            String userId = (String) userData.get("id");
            String email = (String) userData.get("email");

            UserProfile userProfile = syncUserProfile(userData);

            String customToken = generateJwtToken(userId, email);

            UserDTO userDto = UserDTO.builder()
                    .id(userProfile.getId())
                    .email(userProfile.getEmail())
                    .fullName(userProfile.getFullName())
                    .role(userProfile.getRole())
                    .build();

            return AuthResponse.builder()
                    .token(customToken)
                    .session(supabaseResponse)
                    .user(userDto)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process authentication response: " + e.getMessage());
        }
    }

    private Map<String, Object> extractUserInfoFromToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseApiKey);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/user",
                HttpMethod.GET,
                request,
                Map.class
        );

        return response.getBody();
    }

    private UserProfile syncUserProfile(Map<String, Object> userData) {
        String userId = (String) userData.get("id");
        String email = (String) userData.get("email");

        Optional<UserProfile> existingUser = userProfileRepository.findById(UUID.fromString(userId));

        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            UserProfile newUser = new UserProfile();
            newUser.setId(UUID.fromString(userId));
            newUser.setEmail(email);
            newUser.setFullName((String) userData.getOrDefault("user_metadata.full_name", ""));
            newUser.setRole("PARENT"); // Default role

            return userProfileRepository.save(newUser);
        }
    }

    private String generateJwtToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }
}

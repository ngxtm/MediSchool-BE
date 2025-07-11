package com.medischool.backend.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medischool.backend.dto.UserDTO;
import com.medischool.backend.dto.auth.AuthResponse;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.UserProfileRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class SupabaseAuthService {
    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api.key}")
    private String supabaseApiKey;

    @Value("${supabase.api.key.admin}")
    private String supabaseApiKeyAdmin;

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
            
            if (userProfile.getIsActive() != null && !userProfile.getIsActive()) {
                throw new RuntimeException("User account has been deactivated. Please contact administrator.");
            }
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

    public UUID createUserInSupabase(String email, String password, Map<String, Object> userMetadata) {
        log.info("Creating user in Supabase auth.users for email: {}", email);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseApiKeyAdmin);
            headers.set("Authorization", "Bearer " + supabaseApiKeyAdmin);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("email_confirm", true);
            
            if (userMetadata != null && !userMetadata.isEmpty()) {
                requestBody.put("user_metadata", userMetadata);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = supabaseUrl + "/auth/v1/admin/users";
            log.debug("Making request to Supabase URL: {}", url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            log.debug("Supabase create user response status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("id")) {
                    UUID userId = UUID.fromString((String) responseBody.get("id"));
                    log.info("Successfully created user in Supabase: {} with ID: {}", email, userId);
                    return userId;
                } else {
                    throw new RuntimeException("Invalid response from Supabase: missing user ID");
                }
            } else {
                String errorMsg = String.format("Failed to create user in Supabase - Status: %s", response.getStatusCode());
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (HttpClientErrorException e) {
            String errorMsg;
            if (e.getStatusCode() == HttpStatus.CONFLICT || e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                errorMsg = String.format("User %s already exists in Supabase - Status: %s", email, e.getStatusCode());
                log.warn(errorMsg);
                throw new RuntimeException("Email already exists: " + email, e);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorMsg = String.format("Insufficient permissions to create user %s in Supabase - Status: %s. Check service role key permissions.", 
                    email, e.getStatusCode());
            } else {
                errorMsg = String.format("HTTP client error creating user %s in Supabase - Status: %s, Body: %s", 
                    email, e.getStatusCode(), e.getResponseBodyAsString());
            }
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (HttpServerErrorException e) {
            String errorMsg = String.format("Supabase server error creating user %s - Status: %s, Body: %s", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error creating user %s in Supabase: %s", email, e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            log.debug("Finished creating user in Supabase: {}", email);
        }
    }

    public boolean checkUserExistsInSupabase(UUID userId) {
        log.info("Checking if user exists in Supabase: {}", userId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseApiKeyAdmin);
            headers.set("Authorization", "Bearer " + supabaseApiKeyAdmin);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = supabaseUrl + "/auth/v1/admin/users/" + userId.toString();
            log.debug("Checking user existence at URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            log.debug("Supabase check response - Status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("User exists in Supabase: {}", userId);
                return true;
            } else {
                log.warn("Unexpected response status when checking user: {} - Status: {}", userId, response.getStatusCode());
                return false;
            }
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("User does not exist in Supabase: {} - Status: 404", userId);
                return false;
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                String errorMsg = String.format("Insufficient permissions to check user %s in Supabase - Status: %s. Check service role key permissions.", 
                    userId, e.getStatusCode());
                log.error(errorMsg);
                throw new RuntimeException(errorMsg, e);
            } else {
                String errorMsg = String.format("HTTP client error checking user %s in Supabase - Status: %s, Body: %s", 
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
                log.error(errorMsg);
                throw new RuntimeException(errorMsg, e);
            }
            
        } catch (HttpServerErrorException e) {
            String errorMsg = String.format("Supabase server error checking user %s - Status: %s, Body: %s", 
                userId, e.getStatusCode(), e.getResponseBodyAsString());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (ResourceAccessException e) {
            String errorMsg = String.format("Network error connecting to Supabase for user %s: %s", userId, e.getMessage());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error checking user %s in Supabase: %s", userId, e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            log.debug("Finished checking user existence in Supabase: {}", userId);
        }
    }

    public boolean deleteUserFromSupabase(UUID userId) {
        log.info("Starting deletion of user from Supabase: {}", userId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseApiKeyAdmin);
            headers.set("Authorization", "Bearer " + supabaseApiKeyAdmin);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = supabaseUrl + "/auth/v1/admin/users/" + userId.toString();
            log.debug("Attempting to delete user from Supabase using URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    String.class
            );

            log.debug("Supabase delete response - Status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully deleted user from Supabase: {} - Status: {}", userId, response.getStatusCode());
                return true;
            } else {
                String errorMsg = String.format("Supabase deletion failed for user %s - Status: %s, Body: %s", 
                    userId, response.getStatusCode(), response.getBody());
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (HttpClientErrorException e) {
            String errorMsg;
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                errorMsg = String.format("User %s not found in Supabase auth.users table - Status: 404. This may be expected if user was manually deleted from Supabase.", userId);
                log.warn(errorMsg);
                return true;
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorMsg = String.format("Insufficient permissions to delete user %s from Supabase - Status: %s. Check service role key permissions.", 
                    userId, e.getStatusCode());
            } else {
                errorMsg = String.format("HTTP client error deleting user %s from Supabase - Status: %s, Body: %s", 
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            }
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (HttpServerErrorException e) {
            String errorMsg = String.format("Supabase server error deleting user %s - Status: %s, Body: %s", 
                userId, e.getStatusCode(), e.getResponseBodyAsString());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (ResourceAccessException e) {
            String errorMsg = String.format("Network error connecting to Supabase for user %s: %s", userId, e.getMessage());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
            
        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error deleting user %s from Supabase: %s", userId, e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            log.debug("Finished deletion attempt for user: {}", userId);
        }
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
            UserProfile user = existingUser.get();
            
            if (user.getIsActive() != null && !user.getIsActive()) {
                throw new RuntimeException("User account has been deactivated. Please contact administrator.");
            }
            
            return user;
        } else {
            UserProfile newUser = new UserProfile();
            newUser.setId(UUID.fromString(userId));
            newUser.setEmail(email);
            newUser.setFullName((String) userData.getOrDefault("user_metadata.full_name", ""));
            newUser.setRole("PARENT");

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

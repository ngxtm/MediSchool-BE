package com.medischool.backend.security;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.UserProfileRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;
    private final UserProfileRepository userProfileRepository;

    public JwtAuthenticationFilter(String jwtSecret, UserProfileRepository userProfileRepository) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring("Bearer ".length());
                Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

                String userId = claims.getSubject();

                if (userId != null) {
                    Optional<UserProfile> userOpt = userProfileRepository.findById(UUID.fromString(userId));
                    if (userOpt.isPresent()) {
                        UserProfile user = userOpt.get();
                        
                        if (user.getIsActive() != null && !user.getIsActive() || user.getDeletedAt() != null) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("User account has been deactivated or deleted. Please contact administrator.");
                            return;
                        }
                        
                        String role = user.getRole();
                        List<SimpleGrantedAuthority> authorities =
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                        Authentication authentication =
                                new UsernamePasswordAuthenticationToken(userId, token, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT: " + e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}

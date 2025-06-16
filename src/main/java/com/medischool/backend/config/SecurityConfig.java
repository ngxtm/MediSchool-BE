package com.medischool.backend.config;

import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JwtAuthenticationFilter(jwtSecret, userProfileRepository), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/swagger-config",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        .requestMatchers("/api/students/**").permitAll()
                        .requestMatchers("/students/**").permitAll()
                        .requestMatchers("/context-path/**").permitAll()
                        .requestMatchers("/api/me").permitAll()

                        .requestMatchers("/api/vaccines/**").permitAll()
                        .requestMatchers("/api/vaccine-events/**").permitAll()
                        

                        .requestMatchers("/api/medications/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/manager/**").hasAuthority("MANAGER")
                        .requestMatchers("/api/nurse/**").hasAuthority("NURSE")
                        .requestMatchers("/api/vaccines/**").permitAll()
                        .requestMatchers("/api/parent/**").hasAuthority("PARENT")
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("Unauthorized");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().write("Access Denied");
                        }));

        return http.build();
    }
}
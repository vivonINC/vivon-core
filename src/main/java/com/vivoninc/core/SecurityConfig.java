package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, CorsConfigurationSource corsConfigSrc) {
        this.jwtFilter = jwtFilter;
        this.corsConfigurationSource = corsConfigSrc;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // CORS MUST be configured FIRST - this is critical
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // CORS preflight requests - MUST be first and most permissive
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints
                .requestMatchers("/", "/health", "/error").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Auth endpoints - these should be accessible without authentication
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll()
                // WebSocket
                .requestMatchers("/ws/**").permitAll()
                // Test endpoint (remove after testing)
                .requestMatchers("/api/test").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Disable form login and HTTP basic
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            // Add JWT filter AFTER all the CORS and public endpoint handling
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;  // Add this import if missing
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

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtFilter = jwtFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("SECURITY CONFIG: Configuring security filter chain with CORS enabled");
        
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))  // Enables CorsFilter early
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> {
                authz
                    // CRITICAL: Permit ALL OPTIONS preflights FIRST (bypasses auth/JWT)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Then public endpoints
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/health", "/error", "/favicon.ico").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()  // Covers POST/GET for auth
                    .requestMatchers("/api/test").permitAll()
                    .requestMatchers("/debug/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    // Protected: everything else authenticated
                    .anyRequest().authenticated();
                
                System.out.println("SECURITY CONFIG: Authorization rules configured - OPTIONS preflights permitted");
            })
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
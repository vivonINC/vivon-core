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
        System.out.println("SECURITY CONFIG: Configuring security filter chain");
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> {
                csrf.disable();
                System.out.println("SECURITY CONFIG: CSRF disabled");
            })
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                System.out.println("SECURITY CONFIG: Session management set to stateless");
            })
            .authorizeHttpRequests(authz -> {
                authz.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(        "/", 
        "/health",
        "/error",
        "/actuator/health",
        "/api/auth/**", 
        "/ws/**").permitAll();
                authz.anyRequest().authenticated();
                System.out.println("SECURITY CONFIG: Authorization rules configured - /api/auth/** permitted, others authenticated");
            })
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        System.out.println("SECURITY CONFIG: Filter chain built successfully");
        return http.build();
    }

}
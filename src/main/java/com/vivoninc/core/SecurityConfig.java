package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("SECURITY CONFIG: Configuring security filter chain - CORS handled by servlet filter");
        
        return http
            .cors(cors -> cors.disable()) // CORS handled by our servlet filter
            .csrf(csrf -> {
                csrf.disable();
                System.out.println("SECURITY CONFIG: CSRF disabled");
            })
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                System.out.println("SECURITY CONFIG: Session management set to stateless");
            })
            .authorizeHttpRequests(authz -> {
                authz
                    // Public endpoints - be very explicit
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/health", "/error", "/favicon.ico").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    // Auth endpoints - be explicit about methods and paths
                    .requestMatchers("GET", "/api/auth/**").permitAll()
                    .requestMatchers("POST", "/api/auth/**").permitAll()
                    .requestMatchers("OPTIONS", "/api/auth/**").permitAll()
                    // Test endpoint
                    .requestMatchers("GET", "/api/test").permitAll()
                    .requestMatchers("OPTIONS", "/api/test").permitAll()
                    // WebSocket
                    .requestMatchers("/ws/**").permitAll()
                    // Everything else requires authentication
                    .anyRequest().authenticated();
                
                System.out.println("SECURITY CONFIG: Authorization rules configured");
            })
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
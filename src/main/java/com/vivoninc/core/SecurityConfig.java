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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use patterns for flexibility (e.g., allows https://vivon-app.onrender.com with any port/subdomain)
        configuration.setAllowedOriginPatterns(List.of("https://vivon-app.onrender.com", "http://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);  // If you need cookies/auth headers
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("SECURITY CONFIG: Building security filter chain...");
        
        return http
            // CORS must come BEFORE authorizeHttpRequests to process preflights early
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF
            .csrf(csrf -> csrf.disable())
            
            // Configure sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules
            .authorizeHttpRequests(auth -> {
                auth
                    // Permit ALL OPTIONS (preflight) without auth - this is crucial
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    
                    // Public endpoints
                    .requestMatchers("/", "/health", "/error", "/favicon.ico").permitAll()
                    
                    // API endpoints that don't need auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/test").permitAll()
                    .requestMatchers("/debug/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    
                    // Everything else needs authentication
                    .anyRequest().authenticated();
                    
                System.out.println("SECURITY CONFIG: Authorization configured");
            })
            
            // Disable default auth methods
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // Add our JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            
            .build();
    }
}
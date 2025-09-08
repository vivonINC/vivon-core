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
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Allow all origins for now
        configuration.setAllowedMethods(Arrays.asList("*")); // Allow all methods
        configuration.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("SECURITY CONFIG: Building security filter chain...");
        
        return http
            // Enable CORS first - this is crucial
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF
            .csrf(csrf -> csrf.disable())
            
            // Configure sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization - OPTIONS MUST BE FIRST
            .authorizeHttpRequests(auth -> {
                auth
                    // CRITICAL: Allow ALL OPTIONS requests without any authentication
                    .requestMatchers(HttpMethod.OPTIONS).permitAll()
                    
                    // Public endpoints
                    .requestMatchers(
                        "/", 
                        "/health", 
                        "/error", 
                        "/favicon.ico"
                    ).permitAll()
                    
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
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
        System.out.println("SECURITY CONFIG: Configuring security filter chain");
        
        http
            // Disable CSRF for API
            .csrf(csrf -> csrf.disable())
            
            // Configure session management
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization
            .authorizeHttpRequests(authz -> {
                authz
                    // Allow all OPTIONS requests (preflight)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Allow public paths
                    .requestMatchers("/", "/health", "/error", "/favicon.ico").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/test").permitAll()
                    .requestMatchers("/debug/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    // All other requests need authentication
                    .anyRequest().authenticated();
                
                System.out.println("SECURITY CONFIG: Authorization rules configured");
            })
            
            // Disable form login and basic auth
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // Add JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        System.out.println("SECURITY CONFIG: Filter chain configured successfully");
        return http.build();
    }
}
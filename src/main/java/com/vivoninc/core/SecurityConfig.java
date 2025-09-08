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
    private final CorsConfigurationSource corsConfigurationSource;  // Inject it

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource))  // Use your source explicitly
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
                    // Public endpoints - be very explicit, including OPTIONS for preflights
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Explicitly allow all OPTIONS
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/health", "/error", "/favicon.ico").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    // Auth endpoints - permit all methods
                    .requestMatchers("/api/auth/**").permitAll()
                    // Test/debug endpoints
                    .requestMatchers("/api/test").permitAll()
                    .requestMatchers("/debug/**").permitAll()
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
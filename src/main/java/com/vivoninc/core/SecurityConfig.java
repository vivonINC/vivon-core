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
        configuration.setAllowedOrigins(List.of("*"));  // Use wildcard for origins
        configuration.setAllowedMethods(List.of("*"));  // Or keep your specific list if preferred
        configuration.setAllowedHeaders(List.of("*"));  // Echoes requested headers in response
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);  // Set to false; your JWT setup doesn't need true
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("SECURITY CONFIG: Building security filter chain...");
        
        return http
            // CORS FIRST: Processes preflights before security checks
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF (stateless API)
            .csrf(csrf -> csrf.disable())
            
            // Stateless sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization: OPTIONS FIRST to permit all preflights
            .authorizeHttpRequests(auth -> {
                auth
                    // Permit ALL OPTIONS preflights globally (no auth needed)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    
                    // Public static paths
                    .requestMatchers("/", "/health", "/error", "/favicon.ico").permitAll()
                    
                    // Public API paths (includes /api/auth/register POST)
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/test").permitAll()
                    .requestMatchers("/debug/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    
                    // All else requires auth
                    .anyRequest().authenticated();
                    
                System.out.println("SECURITY CONFIG: Authorization configured - OPTIONS permitted");
            })
            
            // Disable form/basic login
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // JWT filter after CORS/Security
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            
            .build();
    }
}
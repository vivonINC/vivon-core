package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CORS Configuration Source Bean Created ===");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Exact origins
        configuration.setAllowedOrigins(Arrays.asList(
            "https://vivon-app.onrender.com",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        
        configuration.setAllowCredentials(true);
        
        // Explicit headers: Cover Content-Type for JSON preflights + Authorization for JWT
        configuration.setAllowedHeaders(List.of(
            "Content-Type",
            "Authorization",
            "Accept",
            "X-Requested-With",
            "Origin"
        ));
        
        // Methods: OPTIONS first for preflights
        configuration.setAllowedMethods(Arrays.asList(
            "OPTIONS", "GET", "POST", "PUT", "DELETE", "HEAD", "PATCH"
        ));
        
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);
        
        System.out.println("CORS Allowed Origins: " + configuration.getAllowedOrigins());
        System.out.println("CORS Allowed Methods: " + configuration.getAllowedMethods());
        System.out.println("CORS Allowed Headers: " + configuration.getAllowedHeaders());
        System.out.println("CORS Allow Credentials: " + configuration.getAllowCredentials());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("CORS Configuration registered for /**");
        return source;
    }
}
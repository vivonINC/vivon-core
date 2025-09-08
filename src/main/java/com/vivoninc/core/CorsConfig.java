package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CORS Configuration Source Bean Created ===");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Exact origins - no wildcards with credentials=true
        configuration.setAllowedOrigins(Arrays.asList(
            "https://vivon-app.onrender.com",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        
        configuration.setAllowCredentials(true);
        
        // Explicitly allow all common headers, including Content-Type for JSON preflights
        configuration.addAllowedHeader("*");  // This should work, but add explicit if issues
        // configuration.addAllowedHeader("Content-Type");  // Uncomment for exact match if * fails
        
        // Explicit methods - ensure POST and OPTIONS are first
        configuration.setAllowedMethods(Arrays.asList(
            "OPTIONS", "GET", "POST", "PUT", "DELETE", "HEAD", "PATCH"
        ));
        
        // Expose for frontend access
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        configuration.setMaxAge(3600L);
        
        // Debug logs
        System.out.println("CORS Allowed Origins: " + configuration.getAllowedOrigins());
        System.out.println("CORS Allowed Methods: " + configuration.getAllowedMethods());
        System.out.println("CORS Allowed Headers: " + configuration.getAllowedHeaders());
        System.out.println("CORS Allow Credentials: " + configuration.getAllowCredentials());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Applies to all paths, including /api/auth/**
        
        System.out.println("CORS Configuration registered for /**");
        return source;
    }
}
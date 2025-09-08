package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CORS Configuration Source Bean Created ===");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (exact match for Render; add more if needed)
        configuration.setAllowedOrigins(Arrays.asList(
            "https://vivon-app.onrender.com",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        
        // Allow credentials (matches your frontend setup)
        configuration.setAllowCredentials(true);
        
        // Allow all headers
        configuration.addAllowedHeader("*");
        
        // Allow all methods, including OPTIONS for preflights
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Expose headers (add Authorization for JWT)
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type"
        ));
        
        // Max age for preflight caching
        configuration.setMaxAge(3600L);
        
        System.out.println("CORS Allowed Origins: " + configuration.getAllowedOrigins());
        System.out.println("CORS Allowed Methods: " + configuration.getAllowedMethods());
        System.out.println("CORS Allowed Headers: " + configuration.getAllowedHeaders());
        System.out.println("CORS Allow Credentials: " + configuration.getAllowCredentials());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("CORS Configuration registered for pattern: /**");
        return source;
    }

    // REMOVE the entire @Bean for FilterRegistrationBean<CorsFilter> - let Spring Security handle it
}
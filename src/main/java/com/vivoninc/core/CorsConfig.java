package com.vivoninc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CORS Configuration Source Bean Created ===");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins
        configuration.setAllowedOrigins(Arrays.asList(
            "https://vivon-app.onrender.com",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Allow all headers
        configuration.addAllowedHeader("*");
        
        // Allow all methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type"
        ));
        
        // Max age
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

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        System.out.println("=== CORS Filter Registration Bean Created ===");
        
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        
        // Create a debug CORS filter
        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource()) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          jakarta.servlet.http.HttpServletResponse response, 
                                          jakarta.servlet.FilterChain filterChain) 
                                          throws java.io.IOException, jakarta.servlet.ServletException {
                
                String origin = request.getHeader("Origin");
                String method = request.getMethod();
                String uri = request.getRequestURI();
                
                System.out.println("=== CORS Filter Processing ===");
                System.out.println("Method: " + method + ", URI: " + uri + ", Origin: " + origin);
                System.out.println("Request Headers:");
                request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
                    System.out.println("  " + headerName + ": " + request.getHeader(headerName))
                );
                
                super.doFilterInternal(request, response, filterChain);
                
                System.out.println("Response Headers after CORS filter:");
                response.getHeaderNames().forEach(headerName -> 
                    System.out.println("  " + headerName + ": " + response.getHeader(headerName))
                );
                System.out.println("=== CORS Filter Done ===");
            }
        };
        
        registrationBean.setFilter(corsFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        
        System.out.println("CORS Filter registered with order: " + Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
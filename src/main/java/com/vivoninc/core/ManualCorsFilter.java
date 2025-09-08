package com.vivoninc.core;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManualCorsFilter implements Filter {

    private final List<String> allowedOrigins = Arrays.asList(
        "https://vivon-app.onrender.com",
        "http://localhost:5173",
        "http://localhost:3000",
        "http://localhost:8080"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        System.out.println("=== CORS Filter: " + method + " " + path + " from origin: " + origin + " ===");

        // Set CORS headers for allowed origins
        if (origin != null && allowedOrigins.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "Origin, Accept, Content-Type, Authorization, X-Requested-With");
            response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Max-Age", "3600");
            
            System.out.println("CORS headers set for origin: " + origin);
        } else if (origin != null) {
            System.out.println("Origin not allowed: " + origin);
        }

        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("Handling OPTIONS preflight request for: " + path);
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Don't continue the filter chain for OPTIONS
        }

        chain.doFilter(request, response);
    }
}
/**package com.vivoninc.core;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("CORS Servlet Filter initialized - this should appear in logs");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        System.out.println("=== CORS Filter START ===");
        System.out.println("CORS Filter - Processing: " + method + " " + uri + " from origin: " + origin);
        
        // Set CORS headers for all requests from allowed origins
        if (origin != null && (
            origin.equals("https://vivon-app.onrender.com") || 
            origin.equals("http://localhost:5173") || 
            origin.equals("http://localhost:3000") ||
            origin.equals("http://localhost:8080")
        )) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
            response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Max-Age", "3600");
            
            System.out.println("CORS Filter - Added CORS headers for origin: " + origin);
        } else {
            System.out.println("CORS Filter - Origin not in allowed list or null: " + origin);
        }
        
        // Handle OPTIONS requests immediately - don't let them go to Spring Security
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("CORS Filter - Handling OPTIONS request directly: " + uri);
            System.out.println("CORS Filter - Sending 200 OK for OPTIONS, stopping filter chain");
            response.setStatus(HttpServletResponse.SC_OK);
            System.out.println("=== CORS Filter END (OPTIONS) ===");
            return; // Don't continue the filter chain - this is key!
        }
        
        // Continue with the filter chain for non-OPTIONS requests
        System.out.println("CORS Filter - Passing through: " + method + " " + uri + " to next filter");
        System.out.println("=== CORS Filter CONTINUE ===");
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        System.out.println("CORS Servlet Filter destroyed");
    }
}**/
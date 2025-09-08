package com.vivoninc.core;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 1) // Run before CORS filter
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("==== REQUEST LOGGING FILTER INITIALIZED ====");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String origin = request.getHeader("Origin");
        String userAgent = request.getHeader("User-Agent");
        
        System.out.println("\n==== INCOMING REQUEST ====");
        System.out.println("Method: " + method);
        System.out.println("URI: " + uri);
        System.out.println("Origin: " + origin);
        System.out.println("User-Agent: " + (userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "null"));
        System.out.println("Query String: " + request.getQueryString());
        
        // Log all headers
        System.out.println("Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            System.out.println("  " + headerName + ": " + request.getHeader(headerName))
        );
        
        long startTime = System.currentTimeMillis();
        
        // Continue the filter chain
        chain.doFilter(req, res);
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("\n==== RESPONSE ====");
        System.out.println("Status: " + response.getStatus());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Response Headers:");
        response.getHeaderNames().forEach(headerName -> 
            System.out.println("  " + headerName + ": " + response.getHeader(headerName))
        );
        System.out.println("==== REQUEST COMPLETE ====\n");
    }

    @Override
    public void destroy() {
        System.out.println("==== REQUEST LOGGING FILTER DESTROYED ====");
    }
}

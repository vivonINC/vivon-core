package com.vivoninc.core;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OptionsLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("=== OPTIONS request received by backend ===");
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Origin: " + request.getHeader("Origin"));
            System.out.println("Access-Control-Request-Method: " +
                    request.getHeader("Access-Control-Request-Method"));
            System.out.println("Access-Control-Request-Headers: " +
                    request.getHeader("Access-Control-Request-Headers"));
        }

        filterChain.doFilter(request, response);
    }
}

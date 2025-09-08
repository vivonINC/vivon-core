package com.vivoninc.core;

import java.io.IOException;
import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTutil jwtUtil;

    public JwtAuthenticationFilter(JWTutil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("JWT Filter Check: " + method + " " + path);
        
        // Skip OPTIONS requests completely
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("JWT Filter: Skipping OPTIONS request");
            return true;
        }
        
        // Skip public paths
        if (path == null || 
            path.equals("/") || 
            path.startsWith("/api/auth") || 
            path.startsWith("/debug") ||
            path.equals("/health") ||
            path.equals("/healthz") ||
            path.equals("/error") ||
            path.equals("/favicon.ico") ||
            path.startsWith("/actuator") ||
            path.equals("/api/test") ||
            path.startsWith("/ws")) {
            
            System.out.println("JWT Filter: Skipping public path: " + path);
            return true;
        }
        
        System.out.println("JWT Filter: Will process (needs auth): " + path);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(request.getMethod().equals("OPTIONS")){
            return;
        }
        System.out.println("JWT Filter: Processing request: " + request.getRequestURI());
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                Integer userId = claims.get("userId", Integer.class);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("JWT Filter: Authenticated user: " + userId);
            } catch (JwtException e) {
                System.out.println("JWT Filter: Invalid token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}

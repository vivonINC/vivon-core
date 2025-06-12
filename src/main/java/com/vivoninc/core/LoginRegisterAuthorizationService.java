package com.vivoninc.core;

import java.util.Collections;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vivoninc.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class LoginRegisterAuthorizationService {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JWTutil jwTutil;

    public LoginRegisterAuthorizationService(JdbcTemplate jdbcTemplate, JWTutil jwTutil) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwTutil = jwTutil;
    }

    public boolean register(String username, String email, String pass) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        if (pass == null || pass.length() < 6) {
            return false;
        }

        String encryptedPass = passwordEncoder.encode(pass);
        jdbcTemplate.update(
            "INSERT INTO users (username, email, password) VALUES (?, ?, ?)",
            username, email, encryptedPass
        );

        return true;
    }

    public String login(String email, String password) {
    try {
        User user = jdbcTemplate.queryForObject(
            "SELECT id, password FROM users WHERE email = ?",
            (rs, rowNum) -> new User(rs.getInt("id"), email, rs.getString("password")),
            email
        );

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwTutil.generateToken(user.getId());
        }
    } catch (EmptyResultDataAccessException e) {
        // no user found
    }
    return null;
}

public Integer validateTokenAndGetUserId(String token) {
    try {
        Claims claims = jwTutil.parseToken(token); // You'll need to implement parseToken()
        return claims.get("userId", Integer.class);
    } catch (JwtException e) {
        return null; // Invalid token
    }
}

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTutil jwtUtil;

    public JwtAuthenticationFilter(JWTutil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, java.io.IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token); // Should validate signature & expiration
                Integer userId = claims.get("userId", Integer.class);

                // You can create a dummy Authentication or use a UserDetailsService
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                // Token invalid, do nothing (request will fail security check later)
            }
        }

        filterChain.doFilter(request, response);
    }
}



}

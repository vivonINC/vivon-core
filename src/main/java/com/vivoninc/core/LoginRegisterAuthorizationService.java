package com.vivoninc.core;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;

import org.neo4j.cypherdsl.core.KeyValueMapEntry;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vivoninc.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.lang.NonNull;
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
    private final Neo4jClient neo4jClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JWTutil jwTutil;

    public LoginRegisterAuthorizationService(JdbcTemplate jdbcTemplate, JWTutil jwTutil, Neo4jClient neo4jClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwTutil = jwTutil;
        this.neo4jClient = neo4jClient;
    }

    @Transactional("neo4jTransactionManager")
    public void createUserNode(int userId) {
        System.out.println("Creating Neo4j node for user id: " + userId);
        
        try {
            String cypher = """
                MERGE (u:User {id: $userId})
                RETURN u.id
                """;

            var result = neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .fetch()
                .all();
                
            System.out.println("Neo4j user node created: " + result);
            
        } catch (Exception e) {
            System.err.println("Error creating Neo4j user node: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String register(String username, String email, String pass) {
        if (email == null || !email.contains("@")) {
            return "Not a valid email";
        }
        if (pass == null || pass.length() < 6) {
            return "Password must be at least 6 characters";
        }
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email
        );
        if (count != null && count > 0) {
            return "Email already in use";
        }

       Integer usernameCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username
        );
        if (usernameCount != null && usernameCount > 0) {
            return "Username already in use";
        }

        String encryptedPass = passwordEncoder.encode(pass);
        
        // Insert into MySQL and get the generated ID
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, encryptedPass);
            return ps;
        }, keyHolder);

        // Create Neo4j node
        createUserNode(keyHolder.getKey().intValue());
        
        return "Registration successful";
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
            Claims claims = jwTutil.parseToken(token);
            return claims.get("userId", Integer.class);
        } catch (JwtException e) {
            return null; // Invalid token
        }
    }
}

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTutil jwtUtil;

    public JwtAuthenticationFilter(JWTutil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean isAuthPath = path.startsWith("/api/auth/");
        boolean isOptions = "OPTIONS".equalsIgnoreCase(request.getMethod());
        boolean shouldSkip = isAuthPath || isOptions;
        System.out.println("Request path: " + path + ", method: " + request.getMethod() + ", shouldNotFilter: " + shouldSkip);
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, java.io.IOException {

        System.out.println("ERROR: doFilterInternal called for: " + request.getRequestURI() + " (this should NOT happen for api/auth/login)");
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                Integer userId = claims.get("userId", Integer.class);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                // Invalid token; optionally log
                System.out.println("Invalid JWT token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
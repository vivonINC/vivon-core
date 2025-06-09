package com.vivoninc.core;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.vivoninc.model.User;

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


}

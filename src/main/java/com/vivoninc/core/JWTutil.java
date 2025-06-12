package com.vivoninc.core;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTutil {

    private final Key key;

    public JWTutil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateToken(int userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    public String validateTokenAndGetSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key) // previously wrong: "secretKey" (undefined)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

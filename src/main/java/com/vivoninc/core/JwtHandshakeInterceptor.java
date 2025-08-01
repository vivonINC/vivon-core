package com.vivoninc.core;

import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTutil jwtUtil;

    public JwtHandshakeInterceptor(JWTutil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        URI uri = request.getURI(); // ws://localhost:8080/ws/chat?token=abc
        String query = uri.getQuery(); // token=abc.def.ghi

        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1];

            try {
                var claims = jwtUtil.parseToken(token);
                Integer userId = claims.get("userId", Integer.class);
                attributes.put("myID", userId.toString()); // Save in session attributes
                return true;
            } catch (Exception e) {
                System.err.println("Invalid token during WebSocket handshake: " + e.getMessage());
                return false; // Reject handshake
            }
        }

        return false; // No token => reject handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}

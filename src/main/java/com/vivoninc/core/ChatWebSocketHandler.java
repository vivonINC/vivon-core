package com.vivoninc.core;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> conversationSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session); // Extract from session attributes
        sessions.put(userId, session);
        System.out.println("User " + userId + " connected");
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        sessions.remove(userId);
        // Remove from all conversations
        conversationSessions.values().forEach(set -> set.remove(userId));
        System.out.println("User " + userId + " disconnected");
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserId(session);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(message.getPayload());
        
        String action = jsonNode.get("action").asText();
        
        switch (action) {
            case "join_conversation":
                joinConversation(userId, jsonNode.get("conversationId").asText());
                break;
            case "leave_conversation":
                leaveConversation(userId, jsonNode.get("conversationId").asText());
                break;
        }
    }
    
    private void joinConversation(String userId, String conversationId) {
        conversationSessions.computeIfAbsent(conversationId, k -> new HashSet<>()).add(userId);
    }
    
    private void leaveConversation(String userId, String conversationId) {
        Set<String> users = conversationSessions.get(conversationId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                conversationSessions.remove(conversationId);
            }
        }
    }
    
    public void broadcastMessage(String conversationId, Object message) {
        Set<String> userIds = conversationSessions.get(conversationId);
        if (userIds != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonMessage = mapper.writeValueAsString(message);
                TextMessage textMessage = new TextMessage(jsonMessage);
                
                userIds.forEach(userId -> {
                    WebSocketSession session = sessions.get(userId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (Exception e) {
                            System.err.println("Error sending message to user " + userId + ": " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }
    
    private String getUserId(WebSocketSession session) {
        return session.getAttributes().get("myID").toString();
    }
}

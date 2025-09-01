package com.vivoninc.core;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.vivoninc.DAOs.MessageDAO;
import com.vivoninc.model.Message;
import com.vivoninc.core.ChatWebSocketHandler;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {
    
    @Autowired
    private MessageDAO messageDAO;
    
    @Autowired
    private ChatWebSocketHandler webSocketHandler;
    
    public void sendMessage(Message message) {
        // Save to database
        messageDAO.send(message);
        
        // Create response object with user info for broadcasting
        Map<String, Object> messageResponse = new HashMap<>();
        messageResponse.put("id", message.getID());
        messageResponse.put("content", message.getContent());
        messageResponse.put("sender_id", message.getSenderID());
        messageResponse.put("created_at", message.getDateSent());
        messageResponse.put("type", message.getType());
        // Add username and avatar from database query
        
        // Broadcast to all users in the conversation
        webSocketHandler.broadcastMessage(String.valueOf(message.getConversationID()), messageResponse);
    }
}

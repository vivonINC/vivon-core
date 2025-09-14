package com.vivoninc.core;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.vivoninc.DAOs.MessageDAO;
import com.vivoninc.DAOs.UserDAO;
import com.vivoninc.model.Message;
import com.vivoninc.core.ChatWebSocketHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    
    @Autowired
    private MessageDAO messageDAO;
    
    @Autowired
    private UserDAO userDAO; // You'll need this to get username/avatar
    
    @Autowired
    private ChatWebSocketHandler webSocketHandler;
    
    public void sendMessage(Message message) {
        // Save to database first - DAO now returns the message with ID
        Message savedMessage = messageDAO.send(message);
        
        Collection<Map<String, Object>> usernameAvatar =
            userDAO.getUserNameAndAvatar(
                List.of(message.getSenderID()) // simplest
            );

        Map<String, Object> userInfo = usernameAvatar.iterator().next();
        // Create response object with complete user info for broadcasting
        Map<String, Object> messageResponse = new HashMap<>();
        messageResponse.put("id", savedMessage.getID()); // Use saved message ID
        messageResponse.put("content", savedMessage.getContent());
        messageResponse.put("sender_id", savedMessage.getSenderID());
        messageResponse.put("created_at", savedMessage.getDateSent());
        messageResponse.put("type", savedMessage.getType());
        messageResponse.put("username", userInfo.get("username"));
        messageResponse.put("avatar", userInfo.get("avatar"));
        
        // Broadcast to all users in the conversation
        webSocketHandler.broadcastMessage(String.valueOf(savedMessage.getConversationID()), messageResponse);
    }
}
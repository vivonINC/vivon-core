package com.vivoninc.core;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.vivoninc.DAOs.MessageDAO;
import com.vivoninc.DAOs.UserDAO;
import com.vivoninc.model.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    
    @Autowired
    private MessageDAO messageDAO;
    
    @Autowired
    private UserDAO userDAO;
    
    @Autowired
    private ChatWebSocketHandler webSocketHandler;
    
    public void sendMessage(Message message) {
        Message savedMessage = messageDAO.send(message);
        
        Collection<Map<String, Object>> usernameAvatar =
            userDAO.getUserNameAndAvatar(
                List.of(message.getSenderID())
            );

        Map<String, Object> userInfo = usernameAvatar.iterator().next();
        Map<String, Object> messageResponse = new HashMap<>();
        messageResponse.put("id", savedMessage.getID());
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
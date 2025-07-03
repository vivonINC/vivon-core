package com.vivoninc.controllers;
import java.util.Collection;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.DAOs.MessageDAO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private MessageDAO messageDAO;

    public MessageController(MessageDAO messageDAO){
        this.messageDAO = messageDAO;
    }

    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
    @GetMapping("/last25")
    public Collection<Map<String, Object>> getLast25Messages(@RequestParam int conversationID){
        return messageDAO.getLast25Messages(conversationID);
    }

    @GetMapping("/conversations")
    public Collection<Map<String, Object>> getConversations(@RequestParam int userID) {
        return messageDAO.getConversationGroups(userID);
    }
    
}

package com.vivoninc.controllers;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.DAOs.MessageDAO;
import com.vivoninc.model.Message;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") //Not needed?
public class MessageController {
    private MessageDAO messageDAO;

    public MessageController(MessageDAO messageDAO){
        this.messageDAO = messageDAO;
    }
    
    @GetMapping("/last25")
    public Collection<Map<String, Object>> getLast25Messages(@RequestParam int conversationID){
        return messageDAO.getLast25Messages(conversationID);
    }

    @GetMapping("/getConversationGroups")
    public Collection<Map<String, Object>> getConversationGroups(@RequestParam int userID) {
        return messageDAO.getConversationGroups(userID);
    }

    @GetMapping("/getDirectConversations")
    public Optional<Map<String, Object>> getDirectConversations(@RequestParam int userID, @RequestParam int  friendID) {
        return messageDAO.findDirectConversation(userID, friendID);
    }
    
    @PostMapping("/createConversation")
    public Long createConversation(@RequestParam String type, @RequestParam String name, @RequestParam List<Integer> ids){
        return messageDAO.createConversation(type, name, ids);
    }

    @GetMapping("/conversationExists")
    public boolean conversationExists(@RequestParam int conversationId, @RequestParam int userId) {
    return messageDAO.isUserMemberOfConversation(userId, conversationId);
    }

    @PostMapping("/send")
    public Message postMethodName(@RequestBody Message message) {
        messageDAO.send(message);
        return message;
    }

    @PostMapping("/addToConv")
    public void addUserToConversation(@RequestBody Map<String, Integer> body) {
        messageDAO.addUserToConversation(body.get("userID"), body.get("ConvoID"));
    }
    
    
}

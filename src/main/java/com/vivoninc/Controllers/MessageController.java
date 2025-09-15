package com.vivoninc.Controllers;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.DAOs.MessageDAO;
import com.vivoninc.model.Message;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import com.vivoninc.core.MessageService;



@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private MessageDAO messageDAO;
    private MessageService messageService;

    public MessageController(MessageDAO messageDAO, MessageService messageService){
        this.messageDAO = messageDAO;
        this.messageService = messageService;
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
    public Message sendMessage(@RequestBody Message message) {
        System.out.println("=== Controller received message ===");
        System.out.println("Message: " + message.getContent());
        System.out.println("Calling MessageService...");
        
        messageService.sendMessage(message);
        
        System.out.println("MessageService call completed");
        return message;
    }

    @PostMapping("/addToConv")
    public void addUserToConversation(@RequestBody Map<String, Integer> body) {
        messageDAO.addUserToConversation(body.get("userID"), body.get("ConvoID"));
    }
    
    @GetMapping("/before")
public Collection<Map<String, Object>> getMessagesBefore(
        @RequestParam int conversationID,
        @RequestParam String beforeTimestamp,
        @RequestParam(defaultValue = "25") int limit) {
           return messageDAO.getMessagesBefore(conversationID, beforeTimestamp, limit);
        }
}

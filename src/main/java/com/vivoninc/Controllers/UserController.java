package com.vivoninc.controllers;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.model.User;
import com.vivoninc.DAOs.UserDAO;
import com.vivoninc.model.Message;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserDAO userDAO;

    public UserController(UserDAO userDAO){
        this.userDAO = userDAO;
    }
    
    @GetMapping("/me") //Test in front end
        public String getMyUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = (Integer) authentication.getPrincipal();
        return String.valueOf(userId);
    }

    @GetMapping("/friends") //Should work
        public Collection<User> getUsersFriends(int id){
            return userDAO.getUsersFriends(id);
        }
    
    @GetMapping("/incFriendRequests")
    public Collection<User> getFriendRequests(int id){
        return userDAO.getUsersIncommingFriendReq(id);
    }

    @PostMapping("/sendFriendRequest")
    public void sendFriendRequest(int myID, int friendID){
        userDAO.sendFriendRequest(myID, friendID);
    }

    @PostMapping("/acceptFriendRequest")
    public void acceptFriendRequest(int myID, int friendID){
        userDAO.acceptFriendRequest(myID, friendID);
    }

    @GetMapping("/messages")
    public Collection<Message> getMessages(@RequestParam int myID, @RequestParam int otherID) {
        return userDAO.getMessagesBetweenUsers(myID, otherID);
    }

}

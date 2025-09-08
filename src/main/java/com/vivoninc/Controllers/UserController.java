package com.vivoninc.Controllers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @GetMapping("/friends")
    public Collection<User> getUsersFriends(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = (Integer) authentication.getPrincipal();
        return userDAO.getUsersFriends(userId);
    }
    
    @GetMapping("/incFriendRequests")
    public Collection<User> getFriendRequests(@RequestParam int id){
        return userDAO.getUsersIncommingFriendReq(id);
    }

    @PostMapping("/sendFriendRequest")
    public String sendFriendRequest(@RequestBody Map<String, Object> request){
        String username = (String) request.get("username");
        int myID = (Integer) request.get("myID");
        int id = userDAO.getIDFromUsername(username);
        String resultString = userDAO.sendFriendRequest(myID, id);
        return resultString;
    }

    @PostMapping("/acceptFriendRequest")
    public void acceptFriendRequest(@RequestBody Map<String, Integer> request){
        int myID = request.get("myID");
        int friendID = request.get("friendID");
        userDAO.acceptFriendRequest(myID, friendID);
    }

    @PostMapping("/declineFriendRequest")
    public void declineFriendRequest(@RequestBody Map<String, Integer> request){
        int myID = request.get("myID");
        int friendID = request.get("friendID");
        userDAO.declineFriendRequest(myID, friendID);
    }

    @GetMapping("/getUsernameAndAvatar")
    public Collection<Map<String, Object>> getUsernameAndAvatar(@RequestParam String ids){
        System.out.println("Received IDs: " + ids);
        List<String> idList = Arrays.asList(ids.split(","));
        return userDAO.getUserNameAndAvatar(idList);
    }

    @DeleteMapping("/removeFriend")
    public void removeFriend(@RequestParam int fromID, @RequestParam int toID){
        userDAO.removeFriend(fromID, toID);
    }

    @PutMapping("/addDescription")
    public void setFriendDescription(@RequestBody Map<String, String> request){
        userDAO.addFriendDescription(Integer.valueOf(request.get("myID")), Integer.valueOf(request.get("friendID")), request.get("description"));
    }

    @GetMapping("/getFriendDescription")
    public String getFriendDescription(@RequestParam int fromID, int toID) {
        return userDAO.getFriendDescription(String.valueOf(fromID), String.valueOf(toID));
    }
    
}


package com.vivoninc.Controllers;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.model.User;
import com.vivoninc.DAOs.UserDAO;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserDAO userDAO;

    public UserController(UserDAO userDAO){
        this.userDAO = userDAO;
    }
    
    @GetMapping("/me")
    public User getCurrentUser(){
        //Requires session tokens to implement
        return null;
    }

    @GetMapping("/friends") //Should work
        public Collection<User> getUsersFriends(int id){
            return userDAO.getUsersFriends(0);
        }

}

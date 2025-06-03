package com.vivoninc.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.model.User;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/me")
    public User getCurrentUser(){
        //Requires session tokens to implement
        return null;
    }

}

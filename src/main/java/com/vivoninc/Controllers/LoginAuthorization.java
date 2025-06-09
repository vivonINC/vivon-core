package com.vivoninc.Controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.vivoninc.model.User;
import com.vivoninc.DAOs.UserDAO;
import com.vivoninc.core.LoginRegisterAuthorizationService;

@RestController
@RequestMapping("/auth")
public class LoginAuthorization {

    @Autowired
    private LoginRegisterAuthorizationService authService;

    @PostMapping("/register")
    //@RequestBody instead of @RequestParam?
    public String register(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        boolean success = authService.register(username, email, password);
        return success ? "Registered!" : "Registration failed";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        String jwt = authService.login(email, password);
        return jwt != null ? jwt : "Login failed";
    }

}

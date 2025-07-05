package com.vivoninc.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.DTOs.LoginRequest;
import com.vivoninc.DTOs.*;
import com.vivoninc.core.LoginRegisterAuthorizationService;

@RestController
@RequestMapping("/api/auth")
public class LoginAuthorization {

    @Autowired
    private LoginRegisterAuthorizationService authService; //Not good

    @PostMapping("/register")
    //@RequestBody instead of @RequestParam?
    public String register(@RequestBody Registration request) {
        String result = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
        return result;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String jwt = authService.login(request.getEmail(), request.getPassword());
        if (jwt != null) {
            return ResponseEntity.ok(Map.of("token", jwt));
        } else {
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(Map.of("error", "Login failed"));
        }
    }

}

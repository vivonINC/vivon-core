package com.vivoninc.Controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.DTOs.*;
import com.vivoninc.core.LoginRegisterAuthorizationService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://vivon-app.onrender.com", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}, allowCredentials = "false")
public class LoginAuthorization {

    @Autowired //Not good
    private LoginRegisterAuthorizationService authService; 

    @PostMapping("/register")
    public String register(@RequestBody Registration request) {
        System.out.println("Register request: " + request);
        String result = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
        System.out.println("result: " + result);
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

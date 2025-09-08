package com.vivoninc.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")

public class CorsTestController {
    
    @GetMapping("/test")
    public ResponseEntity<?> testCors() {
        System.out.println("Test endpoint called successfully");
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
    
    // Explicitly handle OPTIONS for debugging
    @RequestMapping(value = "/test", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        System.out.println("OPTIONS request received for /api/test");
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "https://vivon-app.onrender.com")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .build();
    }
}
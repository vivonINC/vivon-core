package com.vivoninc.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/debug")
public class DebugController {
    
    @GetMapping("/cors")
    public ResponseEntity<?> testCors() {
        System.out.println("=== DEBUG: CORS test endpoint called ===");
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("origin", "backend");
        return ResponseEntity.ok(response);
    }
    
    // Explicitly handle OPTIONS for debugging
    @RequestMapping(value = "/cors", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleCorsOptions() {
        System.out.println("=== DEBUG: OPTIONS request received for /debug/cors ===");
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "https://vivon-app.onrender.com")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .build();
    }
    
    @PostMapping("/auth-test")
    public ResponseEntity<?> testAuthEndpoint(@RequestBody(required = false) Map<String, Object> body) {
        System.out.println("=== DEBUG: Auth test endpoint called ===");
        System.out.println("Request body: " + body);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Auth endpoint working");
        response.put("receivedData", body);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }
}
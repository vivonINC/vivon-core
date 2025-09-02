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
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
    
    // Explicitly handle OPTIONS for debugging
    @RequestMapping(value = "/test", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        System.out.println("OPTIONS request received for /api/test");
        return ResponseEntity.ok().build();
    }
}

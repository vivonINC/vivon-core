package com.vivoninc.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;

@RestController
public class DebugController {
    
    @GetMapping("/debug/cors")
    public ResponseEntity<?> debugCors(HttpServletRequest request) {
        System.out.println("=== DEBUG CORS ENDPOINT CALLED ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug CORS endpoint reached");
        response.put("method", request.getMethod());
        response.put("uri", request.getRequestURI());
        response.put("origin", request.getHeader("Origin"));
        response.put("timestamp", System.currentTimeMillis());
        
        // Log all headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            headers.put(headerName, request.getHeader(headerName))
        );
        response.put("headers", headers);
        
        System.out.println("Debug response: " + response);
        return ResponseEntity.ok(response);
    }
    
    // Explicitly handle OPTIONS
    @RequestMapping(value = "/debug/cors", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> debugCorsOptions(HttpServletRequest request) {
        System.out.println("=== DEBUG CORS OPTIONS CALLED ===");
        System.out.println("Origin: " + request.getHeader("Origin"));
        System.out.println("Access-Control-Request-Method: " + request.getHeader("Access-Control-Request-Method"));
        System.out.println("Access-Control-Request-Headers: " + request.getHeader("Access-Control-Request-Headers"));
        
        return ResponseEntity.ok().build();
    }
}

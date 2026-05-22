package com.microgrid.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                     HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            session.setAttribute("username", "admin");
            session.setAttribute("role", "ADMIN");
            response.put("success", true);
            response.put("username", "admin");
            response.put("role", "ADMIN");
            return ResponseEntity.ok(response);
        }

        if ("operator".equals(request.getUsername()) && "operator123".equals(request.getPassword())) {
            session.setAttribute("username", "operator");
            session.setAttribute("role", "OPERATOR");
            response.put("success", true);
            response.put("username", "operator");
            response.put("role", "OPERATOR");
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Invalid credentials.");
        return ResponseEntity.status(401).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Object username = session.getAttribute("username");
        Object role = session.getAttribute("role");

        if (username == null || role == null) {
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        response.put("authenticated", true);
        response.put("username", username);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
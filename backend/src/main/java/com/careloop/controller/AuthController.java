package com.careloop.controller;

import com.careloop.dto.*;
import com.careloop.model.User;
import com.careloop.security.UserContext;
import com.careloop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication endpoints: register and login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Get current user profile with trust score */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("Please login first");
        }
        User user = authService.getProfile(userId);
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("reliabilityScore", user.getReliabilityScore());
        profile.put("cancelCount", user.getCancelCount());
        profile.put("unreliable", user.getUnreliable());
        profile.put("rating", user.getRating());
        return ResponseEntity.ok(profile);
    }
}

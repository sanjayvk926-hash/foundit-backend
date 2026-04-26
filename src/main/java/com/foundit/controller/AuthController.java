package com.foundit.controller;

import com.foundit.dto.request.LoginRequest;
import com.foundit.dto.request.RegisterRequest;
import com.foundit.dto.response.AuthResponse;
import com.foundit.dto.response.BaseResponse;
import com.foundit.model.User;
import com.foundit.repository.UserRepository;
import com.foundit.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(BaseResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BaseResponse.error("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            User user = authService.registerUser(signUpRequest);
            return ResponseEntity.ok(BaseResponse.success("User registered successfully!", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) {
            user.setPasswordHash(null); // Clear password before sending
            return ResponseEntity.ok(BaseResponse.success("User details fetched", user));
        }
        return ResponseEntity.status(401).body(BaseResponse.error("Not authenticated"));
    }
}

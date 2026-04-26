package com.foundit.service;

import com.foundit.dto.request.LoginRequest;
import com.foundit.dto.request.RegisterRequest;
import com.foundit.dto.response.AuthResponse;
import com.foundit.model.User;
import com.foundit.repository.UserRepository;
import com.foundit.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtTokenProvider jwtUtils;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        
        return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getRole().name());
    }

    public User registerUser(RegisterRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setFullName(signUpRequest.getFullName());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        
        if (signUpRequest.getRole() != null && signUpRequest.getRole().equalsIgnoreCase("ADMIN")) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.STUDENT);
        }
        
        user.setPhone(signUpRequest.getPhone());
        user.setDepartment(signUpRequest.getDepartment());

        return userRepository.save(user);
    }
}

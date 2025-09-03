package com.projects.taskmanager.controller;

import com.projects.taskmanager.dto.AuthResponse;
import com.projects.taskmanager.dto.LoginRequest;
import com.projects.taskmanager.dto.RegisterRequest;
import com.projects.taskmanager.model.Role;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.repository.UserRepository;
import com.projects.taskmanager.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(user);

        AuthResponse response = new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()) != null) {
            return ResponseEntity.badRequest().build();
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            return ResponseEntity.badRequest().build();
        }

        // Create new user
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                passwordEncoder.encode(registerRequest.getPassword()),
                Role.USER
        );

        User savedUser = userRepository.save(user);
        String jwt = jwtUtil.generateToken(savedUser);

        AuthResponse response = new AuthResponse(
                jwt,
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}

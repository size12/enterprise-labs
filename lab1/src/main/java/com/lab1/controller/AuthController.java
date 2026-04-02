package com.lab1.controller;

import com.lab1.dto.AuthRequest;
import com.lab1.dto.AuthResponse;
import com.lab1.service.JwtService;
import com.lab1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        userService.registerUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(null, request.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(authentication);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
                .build());
    }
}

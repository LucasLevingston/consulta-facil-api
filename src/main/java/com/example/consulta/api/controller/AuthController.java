package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.auth.LoginResponseDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.api.dto.user.UserResponseDTO;
import com.example.consulta.application.service.AuthService;
import com.example.consulta.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Creates a new user account")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserDTO request) {
        UserResponseDTO response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

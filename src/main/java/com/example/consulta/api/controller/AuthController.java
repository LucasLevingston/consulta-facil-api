package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.ForgotPasswordDTO;
import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.auth.LoginResponseDTO;
import com.example.consulta.api.dto.auth.ResetPasswordDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.api.dto.user.UserResponseDTO;
import com.example.consulta.application.service.AuthService;
import com.example.consulta.application.service.UserService;
import com.example.consulta.application.service.passwordreset.ForgotPasswordService;
import com.example.consulta.application.service.passwordreset.ResetPasswordService;
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
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;

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

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Sends a password reset link to the user's email")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {
        forgotPasswordService.requestReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user's password using a valid token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        resetPasswordService.reset(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}

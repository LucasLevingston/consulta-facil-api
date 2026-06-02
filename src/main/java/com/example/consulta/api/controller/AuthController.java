package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.ForgotPasswordDTO;
import com.example.consulta.api.dto.auth.GoogleLoginRequestDTO;
import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.auth.LoginResponseDTO;
import com.example.consulta.api.dto.auth.MagicLinkRequestDTO;
import com.example.consulta.api.dto.auth.ResetPasswordDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.api.dto.user.UserResponseDTO;
import com.example.consulta.application.port.in.ForgotPasswordUseCase;
import com.example.consulta.application.port.in.GoogleLoginUseCase;
import com.example.consulta.application.port.in.LoginUseCase;
import com.example.consulta.application.port.in.RegisterUserUseCase;
import com.example.consulta.application.port.in.RequestMagicLinkUseCase;
import com.example.consulta.application.port.in.ResetPasswordUseCase;
import com.example.consulta.application.port.in.VerifyMagicLinkUseCase;
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

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUser;
    private final ForgotPasswordUseCase forgotPassword;
    private final ResetPasswordUseCase resetPassword;
    private final RequestMagicLinkUseCase requestMagicLink;
    private final VerifyMagicLinkUseCase verifyMagicLink;
    private final GoogleLoginUseCase googleLogin;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Creates a new user account")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerUser.execute(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Sends a password reset link to the user's email")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {
        forgotPassword.execute(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user's password using a valid token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        resetPassword.execute(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/magic-link")
    @Operation(summary = "Request magic link", description = "Sends a one-time login link to the user's email (max 3/h)")
    public ResponseEntity<Void> requestMagicLink(@Valid @RequestBody MagicLinkRequestDTO request) {
        requestMagicLink.execute(request.email());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/magic-link/verify")
    @Operation(summary = "Verify magic link", description = "Exchanges a magic link token for a JWT")
    public ResponseEntity<LoginResponseDTO> verifyMagicLink(@RequestParam String token) {
        return ResponseEntity.ok(verifyMagicLink.execute(token));
    }

    @PostMapping("/google")
    @Operation(summary = "Google login", description = "Validates a Google id_token and returns a JWT")
    public ResponseEntity<LoginResponseDTO> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO request) {
        return ResponseEntity.ok(googleLogin.execute(request.idToken()));
    }
}

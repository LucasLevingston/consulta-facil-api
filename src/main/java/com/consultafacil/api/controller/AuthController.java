package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.ForgotPasswordDTO;
import com.consultafacil.api.dto.auth.GoogleLoginRequestDTO;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.auth.LoginResponseDTO;
import com.consultafacil.api.dto.auth.MagicLinkRequestDTO;
import com.consultafacil.api.dto.auth.RefreshTokenRequestDTO;
import com.consultafacil.api.dto.auth.ResetPasswordDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.auth.ForgotPasswordUseCase;
import com.consultafacil.application.port.in.auth.GoogleLoginUseCase;
import com.consultafacil.application.port.in.auth.GoogleOAuthCallbackUseCase;
import com.consultafacil.application.port.in.auth.GoogleOAuthRedirectUseCase;
import com.consultafacil.application.port.in.auth.LoginUseCase;
import com.consultafacil.application.port.in.user.RegisterUserUseCase;
import com.consultafacil.application.port.in.auth.RequestMagicLinkUseCase;
import com.consultafacil.application.port.in.auth.ResetPasswordUseCase;
import com.consultafacil.application.port.in.auth.RotateRefreshTokenUseCase;
import com.consultafacil.application.port.in.auth.VerifyMagicLinkUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    private final GoogleOAuthRedirectUseCase googleOAuthRedirect;
    private final GoogleOAuthCallbackUseCase googleOAuthCallback;
    private final RotateRefreshTokenUseCase rotateRefreshToken;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

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
    @Operation(summary = "Google login (id_token)", description = "Validates a Google id_token and returns a JWT")
    public ResponseEntity<LoginResponseDTO> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO request) {
        return ResponseEntity.ok(googleLogin.execute(request.idToken()));
    }

    @GetMapping("/google/redirect")
    @Operation(summary = "Google OAuth redirect", description = "Redirects the browser to Google's OAuth consent screen")
    public ResponseEntity<Void> googleRedirect() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleOAuthRedirect.buildAuthorizationUrl()))
                .build();
    }

    @GetMapping("/google/callback")
    @Operation(summary = "Google OAuth callback", description = "Exchanges the authorization code for a JWT and redirects to the frontend")
    public ResponseEntity<Void> googleCallback(@RequestParam String code) {
        LoginResponseDTO result = googleOAuthCallback.execute(code);
        String redirect = appUrl + "/auth/google-callback?token=" + result.getToken()
                + "&refreshToken=" + result.getRefreshToken();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect))
                .build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Rotates a refresh token and issues new access + refresh tokens")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(rotateRefreshToken.execute(request.refreshToken()));
    }
}

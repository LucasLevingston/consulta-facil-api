package com.example.consulta.api.controller;

import com.example.consulta.api.dto.user.UserResponseDTO;
import com.example.consulta.application.service.UserService;
import com.example.consulta.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDTO response = userService.getUserById(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String userId) {
        UserResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}

package com.consultafacil.api.controller;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.UserUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import com.consultafacil.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAdminListUsers(authentication)")
    @Operation(summary = "List all users (admin only)")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UserRole role) {
        return ResponseEntity.ok(userUseCase.getAllUsers(page, size, role));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canViewUserProfile(authentication)")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userUseCase.getById(userDetails.getUserId()));
    }

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAdminListUsers(authentication)")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userUseCase.getById(userId));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canUpdateUserProfile(authentication)")
    @Operation(summary = "Upload avatar do usuário autenticado")
    public ResponseEntity<UserResponseDTO> uploadAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(userUseCase.uploadAvatar(userDetails.getUserId(), file));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("@adminPolicy.canAdminUpdateUser(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}

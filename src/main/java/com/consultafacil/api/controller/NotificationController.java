package com.consultafacil.api.controller;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.application.port.in.NotificationUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationUseCase notificationUseCase;

    @GetMapping("/me")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationUseCase.getMyNotifications(userDetails.getUserId()));
    }

    @GetMapping("/me/unread-count")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of("count", notificationUseCase.countUnread(userDetails.getUserId())));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationUseCase.markAsRead(notificationId, userDetails.getUserId()));
    }

    @PutMapping("/read-all")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationUseCase.markAllAsRead(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{notificationId}/accept")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Accept a clinic invite notification")
    public ResponseEntity<NotificationResponseDTO> acceptInvite(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationUseCase.acceptInvite(notificationId, userDetails.getUserId()));
    }

    @PutMapping("/{notificationId}/decline")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Decline a clinic invite notification")
    public ResponseEntity<NotificationResponseDTO> declineInvite(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationUseCase.declineInvite(notificationId, userDetails.getUserId()));
    }
}

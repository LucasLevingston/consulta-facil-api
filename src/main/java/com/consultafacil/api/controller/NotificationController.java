package com.consultafacil.api.controller;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.application.port.in.clinic.AcceptInviteUseCase;
import com.consultafacil.application.port.in.notification.CountUnreadNotificationsUseCase;
import com.consultafacil.application.port.in.clinic.DeclineInviteUseCase;
import com.consultafacil.application.port.in.notification.GetMyNotificationsUseCase;
import com.consultafacil.application.port.in.notification.MarkAllNotificationsAsReadUseCase;
import com.consultafacil.application.port.in.notification.MarkNotificationAsReadUseCase;
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

    private final GetMyNotificationsUseCase getMyNotificationsUseCase;
    private final CountUnreadNotificationsUseCase countUnreadNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final MarkAllNotificationsAsReadUseCase markAllNotificationsAsReadUseCase;
    private final AcceptInviteUseCase acceptInviteUseCase;
    private final DeclineInviteUseCase declineInviteUseCase;

    @GetMapping("/me")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getMyNotificationsUseCase.execute(userDetails.getUserId()));
    }

    @GetMapping("/me/unread-count")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of("count", countUnreadNotificationsUseCase.execute(userDetails.getUserId())));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(markNotificationAsReadUseCase.execute(notificationId, userDetails.getUserId()));
    }

    @PutMapping("/read-all")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        markAllNotificationsAsReadUseCase.execute(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{notificationId}/accept")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Accept a clinic invite notification")
    public ResponseEntity<NotificationResponseDTO> acceptInvite(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(acceptInviteUseCase.execute(notificationId, userDetails.getUserId()));
    }

    @PutMapping("/{notificationId}/decline")
    @PreAuthorize("@adminPolicy.canAccessNotifications(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Decline a clinic invite notification")
    public ResponseEntity<NotificationResponseDTO> declineInvite(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(declineInviteUseCase.execute(notificationId, userDetails.getUserId()));
    }
}

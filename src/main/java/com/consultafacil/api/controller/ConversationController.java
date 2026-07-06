package com.consultafacil.api.controller;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.application.port.in.GetConversationHistoryUseCase;
import com.consultafacil.application.port.in.GetOrCreateConversationUseCase;
import com.consultafacil.application.port.in.ListConversationsUseCase;
import com.consultafacil.application.port.in.MarkConversationAsReadUseCase;
import com.consultafacil.core.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ListConversationsUseCase listConversationsUseCase;
    private final GetOrCreateConversationUseCase getOrCreateConversationUseCase;
    private final GetConversationHistoryUseCase getConversationHistoryUseCase;
    private final MarkConversationAsReadUseCase markConversationAsReadUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResponseDTO>> listConversations() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(listConversationsUseCase.execute(userId));
    }

    @PostMapping("/{professionalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponseDTO> getOrCreate(@PathVariable String professionalId) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(getOrCreateConversationUseCase.execute(userId, professionalId));
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MessageResponseDTO>> getHistory(
            @PathVariable String id,
            @PageableDefault(size = 30) Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(getConversationHistoryUseCase.execute(id, userId, pageable));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        markConversationAsReadUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }
}

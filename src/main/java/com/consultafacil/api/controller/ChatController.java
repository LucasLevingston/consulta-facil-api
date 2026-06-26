package com.consultafacil.api.controller;

import com.consultafacil.api.dto.messaging.SendMessageDTO;
import com.consultafacil.application.port.in.ConversationUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ConversationUseCase conversationUseCase;

    @MessageMapping("/chat/{conversationId}")
    public void handleMessage(
            @DestinationVariable String conversationId,
            @Payload SendMessageDTO dto,
            Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated WS message attempt on conversation {}", conversationId);
            return;
        }
        String senderId = ((CustomUserDetails) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUserId();
        conversationUseCase.sendMessage(conversationId, senderId, dto.content());
    }
}

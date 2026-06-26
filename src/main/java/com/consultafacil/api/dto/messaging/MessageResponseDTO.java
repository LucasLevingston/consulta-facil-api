package com.consultafacil.api.dto.messaging;

import java.time.LocalDateTime;

public record MessageResponseDTO(
        String id,
        String senderId,
        String senderName,
        String content,
        LocalDateTime sentAt,
        boolean isRead
) {}

package com.consultafacil.api.dto.messaging;

public record ConversationResponseDTO(
        String id,
        String otherUserId,
        String otherUserName,
        String otherUserImageUrl,
        MessageResponseDTO lastMessage,
        long unreadCount
) {}

package com.consultafacil.domain.port.out.whatsapp;

import com.consultafacil.domain.entity.WhatsAppConversation;

import java.util.Optional;

public interface WhatsAppConversationRepositoryPort {

    WhatsAppConversation save(WhatsAppConversation conversation);

    Optional<WhatsAppConversation> findByPhoneNumber(String phoneNumber);
}

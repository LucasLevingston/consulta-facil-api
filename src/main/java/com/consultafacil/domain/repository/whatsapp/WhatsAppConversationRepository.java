package com.consultafacil.domain.repository.whatsapp;

import com.consultafacil.domain.entity.WhatsAppConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WhatsAppConversationRepository extends JpaRepository<WhatsAppConversation, String> {
    Optional<WhatsAppConversation> findByPhoneNumber(String phoneNumber);
}

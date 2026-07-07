package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationRepositoryPort {
    Conversation save(Conversation conversation);
    Optional<Conversation> findByPatientIdAndProfessionalId(String patientId, String professionalId);
    List<Conversation> findByUserId(String userId);
    Optional<Conversation> findById(String id);
}

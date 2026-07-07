package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.application.port.in.conversation.GetOrCreateConversationUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.conversation.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GetOrCreateConversationService implements GetOrCreateConversationUseCase {

    private final ConversationRepositoryPort conversationRepository;
    private final UserRepositoryPort userRepository;
    private final ConversationMapper mapper;

    @Override
    @Transactional
    public ConversationResponseDTO execute(String requesterId, String professionalId) {
        User requester = findUser(requesterId);
        User professional = findUser(professionalId);

        if (professional.getRole() != UserRole.PROFESSIONAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target user is not a professional");
        }

        Conversation conversation = conversationRepository
                .findByPatientIdAndProfessionalId(requesterId, professionalId)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().patient(requester).professional(professional).build()));

        return mapper.toConversationDTO(conversation, requesterId);
    }

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}

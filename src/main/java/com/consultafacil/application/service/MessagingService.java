package com.consultafacil.application.service;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.application.port.in.ConversationUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.Message;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagingService implements ConversationUseCase {

    private final ConversationRepositoryPort conversationRepository;
    private final MessageRepositoryPort messageRepository;
    private final UserRepositoryPort userRepository;
    private final NotificationRepositoryPort notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ConversationResponseDTO getOrCreateConversation(String requesterId, String professionalId) {
        User requester = findUser(requesterId);
        User professional = findUser(professionalId);

        if (professional.getRole() != UserRole.PROFESSIONAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target user is not a professional");
        }

        String patientId = requesterId;
        Conversation conversation = conversationRepository
                .findByPatientIdAndProfessionalId(patientId, professionalId)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().patient(requester).professional(professional).build()));

        return toConversationDTO(conversation, requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> listConversations(String userId) {
        return conversationRepository.findByUserId(userId).stream()
                .map(c -> toConversationDTO(c, userId))
                .toList();
    }

    @Override
    @Transactional
    public Page<MessageResponseDTO> getHistory(String conversationId, String userId, Pageable pageable) {
        Conversation conversation = findConversation(conversationId);
        assertParticipant(conversation, userId);
        return messageRepository.findByConversationId(conversationId, pageable)
                .map(MessagingService::toMessageDTO);
    }

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(String conversationId, String senderId, String content) {
        Conversation conversation = findConversation(conversationId);
        assertParticipant(conversation, senderId);

        User sender = findUser(senderId);
        Message message = messageRepository.save(
                Message.builder().conversation(conversation).sender(sender).content(content).build());

        MessageResponseDTO dto = toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, dto);

        String recipientId = conversation.getPatient().getId().equals(senderId)
                ? conversation.getProfessional().getId()
                : conversation.getPatient().getId();
        sendNewMessageNotification(sender, recipientId, conversationId);

        return dto;
    }

    @Override
    @Transactional
    public void markAsRead(String conversationId, String userId) {
        Conversation conversation = findConversation(conversationId);
        assertParticipant(conversation, userId);
        messageRepository.markAsRead(conversationId, userId);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void assertParticipant(Conversation c, String userId) {
        boolean isParticipant = c.getPatient().getId().equals(userId)
                || c.getProfessional().getId().equals(userId);
        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private Conversation findConversation(String id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
    }

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void sendNewMessageNotification(User sender, String recipientId, String conversationId) {
        User recipient = findUser(recipientId);
        notificationRepository.save(Notification.builder()
                .type(NotificationType.NEW_MESSAGE)
                .title("Nova mensagem")
                .message(sender.getName() + " te enviou uma mensagem.")
                .targetUser(recipient)
                .status(NotificationStatus.PENDING)
                .build());
    }

    private ConversationResponseDTO toConversationDTO(Conversation c, String viewerId) {
        User other = c.getPatient().getId().equals(viewerId) ? c.getProfessional() : c.getPatient();
        long unread = messageRepository.countUnread(c.getId(), viewerId);

        Page<MessageResponseDTO> lastPage = messageRepository.findByConversationId(
                c.getId(), Pageable.ofSize(1)).map(MessagingService::toMessageDTO);
        MessageResponseDTO lastMessage = lastPage.isEmpty() ? null : lastPage.getContent().get(0);

        return new ConversationResponseDTO(c.getId(), other.getId(), other.getName(),
                other.getImageUrl(), lastMessage, unread);
    }

    private static MessageResponseDTO toMessageDTO(Message m) {
        return new MessageResponseDTO(m.getId(), m.getSender().getId(), m.getSender().getName(),
                m.getContent(), m.getSentAt(), m.getReadAt() != null);
    }
}

package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.application.port.in.SendMessageUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.Message;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    private final MessageRepositoryPort messageRepository;
    private final UserRepositoryPort userRepository;
    private final NotificationRepositoryPort notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationAccessValidator accessValidator;
    private final MessageMapper mapper;

    @Override
    @Transactional
    public MessageResponseDTO execute(String conversationId, String senderId, String content) {
        Conversation conversation = accessValidator.findAccessibleConversation(conversationId, senderId);

        User sender = findUser(senderId);
        Message message = messageRepository.save(
                Message.builder().conversation(conversation).sender(sender).content(content).build());

        MessageResponseDTO dto = mapper.toDTO(message);
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, dto);

        String recipientId = conversation.getPatient().getId().equals(senderId)
                ? conversation.getProfessional().getId()
                : conversation.getPatient().getId();
        sendNewMessageNotification(sender, recipientId);

        return dto;
    }

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void sendNewMessageNotification(User sender, String recipientId) {
        User recipient = findUser(recipientId);
        notificationRepository.save(Notification.builder()
                .type(NotificationType.NEW_MESSAGE)
                .title("Nova mensagem")
                .message(sender.getName() + " te enviou uma mensagem.")
                .targetUser(recipient)
                .status(NotificationStatus.PENDING)
                .build());
    }
}

package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.Message;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.conversation.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.conversation.MessageRepositoryPort;
import com.consultafacil.domain.port.out.notification.NotificationRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendMessageServiceTest {

    @Mock ConversationRepositoryPort conversationRepository;
    @Mock MessageRepositoryPort messageRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock NotificationRepositoryPort notificationRepository;
    @Mock SimpMessagingTemplate messagingTemplate;

    SendMessageService service;

    private User patient() {
        return User.builder().id("patient-1").name("Paciente").role(UserRole.PATIENT).build();
    }

    private User professional() {
        return User.builder().id("prof-1").name("Dr. João").role(UserRole.PROFESSIONAL).build();
    }

    private Conversation conversation(User patient, User professional) {
        return Conversation.builder().id("conv-1").patient(patient).professional(professional)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    @BeforeEach
    void setUp() {
        service = new SendMessageService(messageRepository, userRepository, notificationRepository,
                messagingTemplate, new ConversationAccessValidator(conversationRepository),
                new MessageMapper());
    }

    @Test
    void sendMessage_savesAndBroadcasts() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);
        Message msg = Message.builder().id("msg-1").conversation(conv).sender(pat)
                .content("Olá").sentAt(LocalDateTime.now()).build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));
        when(userRepository.findById("patient-1")).thenReturn(Optional.of(pat));
        when(messageRepository.save(any())).thenReturn(msg);
        when(userRepository.findById("prof-1")).thenReturn(Optional.of(pro));
        when(notificationRepository.save(any())).thenReturn(mock(Notification.class));

        MessageResponseDTO result = service.execute("conv-1", "patient-1", "Olá");

        assertThat(result.content()).isEqualTo("Olá");
        verify(messagingTemplate).convertAndSend(eq("/topic/conversation.conv-1"), any(MessageResponseDTO.class));
        verify(notificationRepository).save(any());
    }

    @Test
    void sendMessage_nonParticipant_throwsForbidden() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> service.execute("conv-1", "other-user", "Oi"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Access denied");
    }
}

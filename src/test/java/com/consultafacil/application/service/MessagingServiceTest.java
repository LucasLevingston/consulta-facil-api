package com.consultafacil.application.service;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.Message;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock ConversationRepositoryPort conversationRepository;
    @Mock MessageRepositoryPort messageRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock NotificationRepositoryPort notificationRepository;
    @Mock SimpMessagingTemplate messagingTemplate;

    @InjectMocks MessagingService service;

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

    // ── getOrCreateConversation ───────────────────────────────────────────────

    @Test
    void getOrCreate_existingConversation_returnsSame() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(userRepository.findById("patient-1")).thenReturn(Optional.of(pat));
        when(userRepository.findById("prof-1")).thenReturn(Optional.of(pro));
        when(conversationRepository.findByPatientIdAndProfessionalId("patient-1", "prof-1"))
                .thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationId(eq("conv-1"), any())).thenReturn(Page.empty());
        when(messageRepository.countUnread("conv-1", "patient-1")).thenReturn(0L);
        when(userRepository.findById("prof-1")).thenReturn(Optional.of(pro));

        ConversationResponseDTO result = service.getOrCreateConversation("patient-1", "prof-1");

        assertThat(result.id()).isEqualTo("conv-1");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void getOrCreate_newConversation_creates() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(userRepository.findById("patient-1")).thenReturn(Optional.of(pat));
        when(userRepository.findById("prof-1")).thenReturn(Optional.of(pro));
        when(conversationRepository.findByPatientIdAndProfessionalId("patient-1", "prof-1"))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenReturn(conv);
        when(messageRepository.findByConversationId(eq("conv-1"), any())).thenReturn(Page.empty());
        when(messageRepository.countUnread("conv-1", "patient-1")).thenReturn(0L);

        service.getOrCreateConversation("patient-1", "prof-1");

        verify(conversationRepository).save(any());
    }

    // ── listConversations ─────────────────────────────────────────────────────

    @Test
    void listConversations_returnsOnlyUserConversations() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findByUserId("patient-1")).thenReturn(List.of(conv));
        when(messageRepository.findByConversationId(eq("conv-1"), any())).thenReturn(Page.empty());
        when(messageRepository.countUnread("conv-1", "patient-1")).thenReturn(2L);

        List<ConversationResponseDTO> result = service.listConversations("patient-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).unreadCount()).isEqualTo(2);
    }

    // ── sendMessage ───────────────────────────────────────────────────────────

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

        MessageResponseDTO result = service.sendMessage("conv-1", "patient-1", "Olá");

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

        assertThatThrownBy(() -> service.sendMessage("conv-1", "other-user", "Oi"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Access denied");
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    void markAsRead_updatesReadAt() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));

        service.markAsRead("conv-1", "patient-1");

        verify(messageRepository).markAsRead("conv-1", "patient-1");
    }

    // ── getHistory ────────────────────────────────────────────────────────────

    @Test
    void getHistory_nonParticipant_throwsForbidden() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> service.getHistory("conv-1", "outsider", Pageable.unpaged()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getHistory_participant_returnsMessages() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);
        Message msg = Message.builder().id("msg-1").conversation(conv).sender(pat)
                .content("Hi").sentAt(LocalDateTime.now()).build();

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationId(eq("conv-1"), any()))
                .thenReturn(new PageImpl<>(List.of(msg)));

        Page<MessageResponseDTO> result = service.getHistory("conv-1", "patient-1", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Hi");
    }
}

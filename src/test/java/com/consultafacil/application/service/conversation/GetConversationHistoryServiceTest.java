package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.Message;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.conversation.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.conversation.MessageRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConversationHistoryServiceTest {

    @Mock ConversationRepositoryPort conversationRepository;
    @Mock MessageRepositoryPort messageRepository;

    GetConversationHistoryService service;

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
        service = new GetConversationHistoryService(messageRepository,
                new ConversationAccessValidator(conversationRepository),
                new MessageMapper());
    }

    @Test
    void getHistory_nonParticipant_throwsForbidden() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> service.execute("conv-1", "outsider", Pageable.unpaged()))
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

        Page<MessageResponseDTO> result = service.execute("conv-1", "patient-1", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Hi");
    }
}

package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListConversationsServiceTest {

    @Mock ConversationRepositoryPort conversationRepository;
    @Mock MessageRepositoryPort messageRepository;

    ListConversationsService service;

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
        service = new ListConversationsService(conversationRepository,
                new ConversationMapper(messageRepository, new MessageMapper()));
    }

    @Test
    void listConversations_returnsOnlyUserConversations() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findByUserId("patient-1")).thenReturn(List.of(conv));
        when(messageRepository.findByConversationId(any(), any())).thenReturn(Page.empty());
        when(messageRepository.countUnread("conv-1", "patient-1")).thenReturn(2L);

        List<ConversationResponseDTO> result = service.execute("patient-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).unreadCount()).isEqualTo(2);
    }
}

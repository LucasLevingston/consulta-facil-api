package com.consultafacil.application.service.conversation;

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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkConversationAsReadServiceTest {

    @Mock ConversationRepositoryPort conversationRepository;
    @Mock MessageRepositoryPort messageRepository;

    MarkConversationAsReadService service;

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
        service = new MarkConversationAsReadService(messageRepository,
                new ConversationAccessValidator(conversationRepository));
    }

    @Test
    void markAsRead_updatesReadAt() {
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv));

        service.execute("conv-1", "patient-1");

        verify(messageRepository).markAsRead("conv-1", "patient-1");
    }
}

package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.conversation.ConversationRepositoryPort;
import com.consultafacil.domain.port.out.conversation.MessageRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateConversationServiceTest {

    @Mock ConversationRepositoryPort conversationRepository;
    @Mock MessageRepositoryPort messageRepository;
    @Mock UserRepositoryPort userRepository;

    GetOrCreateConversationService service;

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

    void setUp() {
        service = new GetOrCreateConversationService(conversationRepository, userRepository,
                new ConversationMapper(messageRepository, new MessageMapper()));
    }

    @Test
    void getOrCreate_existingConversation_returnsSame() {
        setUp();
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(userRepository.findById("patient-1")).thenReturn(Optional.of(pat));
        when(userRepository.findById("prof-1")).thenReturn(Optional.of(pro));
        when(conversationRepository.findByPatientIdAndProfessionalId("patient-1", "prof-1"))
                .thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationId(any(), any())).thenReturn(Page.empty());
        when(messageRepository.countUnread("conv-1", "patient-1")).thenReturn(0L);

        ConversationResponseDTO result = service.execute("patient-1", "prof-1");

        assertThat(result.id()).isEqualTo("conv-1");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void getOrCreate_newConversation_creates() {
        setUp();
        User pat = patient();
        User pro = professional();
        Conversation conv = conversation(pat, pro);

        when(userRepository.findById("patient-1")).thenReturn(Optional.of(pat));
        when(userRepository.findById("prof-1")).thenReturn(Optional.of(pro));
        when(conversationRepository.findByPatientIdAndProfessionalId("patient-1", "prof-1"))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenReturn(conv);
        when(messageRepository.findByConversationId(any(), any())).thenReturn(Page.empty());
        when(messageRepository.countUnread("conv-1", "patient-1")).thenReturn(0L);

        service.execute("patient-1", "prof-1");

        verify(conversationRepository).save(any());
    }
}

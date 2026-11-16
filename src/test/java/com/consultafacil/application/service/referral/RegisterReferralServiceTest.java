package com.consultafacil.application.service.referral;

import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.referral.ReferralCodeRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterReferralServiceTest {

    @Mock ReferralCodeRepositoryPort referralCodeRepository;
    @Mock ReferralRepositoryPort referralRepository;

    RegisterReferralService service;

    @BeforeEach
    void setUp() {
        service = new RegisterReferralService(referralCodeRepository, referralRepository);
    }

    private ReferralCode code(boolean active) {
        return ReferralCode.builder().id("code-1").userId("referrer-1").code("ABC123").active(active).build();
    }

    @Test
    void execute_linksReferredUserToCodeOwner() {
        when(referralCodeRepository.findByCode("ABC123")).thenReturn(Optional.of(code(true)));
        when(referralRepository.findByReferredId("new-user")).thenReturn(Optional.empty());

        service.execute("new-user", "ABC123");

        ArgumentCaptor<Referral> saved = ArgumentCaptor.forClass(Referral.class);
        verify(referralRepository).save(saved.capture());
        assertThat(saved.getValue().getReferrerId()).isEqualTo("referrer-1");
        assertThat(saved.getValue().getReferredId()).isEqualTo("new-user");
        assertThat(saved.getValue().getReferralCodeId()).isEqualTo("code-1");
    }

    @Test
    void execute_rejectsUnknownCode() {
        when(referralCodeRepository.findByCode("NOPE00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("new-user", "NOPE00"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(referralRepository, never()).save(any());
    }

    @Test
    void execute_rejectsInactiveCode() {
        when(referralCodeRepository.findByCode("ABC123")).thenReturn(Optional.of(code(false)));

        assertThatThrownBy(() -> service.execute("new-user", "ABC123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inativo");
        verify(referralRepository, never()).save(any());
    }

    @Test
    void execute_rejectsSelfReferral() {
        when(referralCodeRepository.findByCode("ABC123")).thenReturn(Optional.of(code(true)));

        assertThatThrownBy(() -> service.execute("referrer-1", "ABC123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Auto-indicação");
        verify(referralRepository, never()).save(any());
    }

    @Test
    void execute_rejectsUserThatWasAlreadyReferred() {
        when(referralCodeRepository.findByCode("ABC123")).thenReturn(Optional.of(code(true)));
        when(referralRepository.findByReferredId("new-user"))
                .thenReturn(Optional.of(Referral.builder().id("old").build()));

        assertThatThrownBy(() -> service.execute("new-user", "ABC123"))
                .isInstanceOf(DuplicateResourceException.class);
        verify(referralRepository, never()).save(any());
    }
}

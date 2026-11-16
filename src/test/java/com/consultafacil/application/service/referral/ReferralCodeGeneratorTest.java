package com.consultafacil.application.service.referral;

import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.referral.ReferralCodeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralCodeGeneratorTest {

    @Mock ReferralCodeRepositoryPort referralCodeRepository;

    ReferralCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ReferralCodeGenerator(referralCodeRepository);
    }

    @Test
    void generateAndSave_persistsSixCharUppercaseAlphanumericCodeForUser() {
        when(referralCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(referralCodeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReferralCode result = generator.generateAndSave("user-1");

        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getCode()).matches("[A-Z0-9]{6}");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void generateAndSave_retriesUntilCodeIsUnique() {
        when(referralCodeRepository.existsByCode(anyString())).thenReturn(true, true, false);
        when(referralCodeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        generator.generateAndSave("user-1");

        verify(referralCodeRepository, times(3)).existsByCode(anyString());
        ArgumentCaptor<ReferralCode> saved = ArgumentCaptor.forClass(ReferralCode.class);
        verify(referralCodeRepository).save(saved.capture());
        assertThat(saved.getValue().getCode()).hasSize(6);
    }

    @Test
    void generateAndSave_givesUpAfterTenCollisions() {
        when(referralCodeRepository.existsByCode(anyString())).thenReturn(true);

        assertThatThrownBy(() -> generator.generateAndSave("user-1"))
                .isInstanceOf(IllegalStateException.class);
        verify(referralCodeRepository, times(10)).existsByCode(anyString());
        verify(referralCodeRepository, never()).save(any());
    }
}

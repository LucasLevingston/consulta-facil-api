package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.referral.ReferralCodeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegenerateReferralCodeServiceTest {

    @Mock ReferralCodeRepositoryPort referralCodeRepository;
    @Mock ReferralCodeGenerator codeGenerator;

    RegenerateReferralCodeService service;

    @BeforeEach
    void setUp() {
        service = new RegenerateReferralCodeService(referralCodeRepository, codeGenerator, new ReferralCodeMapper());
    }

    @Test
    void execute_deactivatesCurrentCodeBeforeGeneratingANewOne() {
        ReferralCode current = ReferralCode.builder().id("rc-1").userId("user-1").code("OLD123").build();
        when(referralCodeRepository.findByUserId("user-1")).thenReturn(Optional.of(current));
        when(codeGenerator.generateAndSave("user-1"))
                .thenReturn(ReferralCode.builder().id("rc-2").userId("user-1").code("NEW456").build());

        ReferralCodeDTO result = service.execute("user-1");

        assertThat(current.isActive()).isFalse();
        InOrder order = inOrder(referralCodeRepository, codeGenerator);
        order.verify(referralCodeRepository).save(current);
        order.verify(codeGenerator).generateAndSave("user-1");
        assertThat(result.getCode()).isEqualTo("NEW456");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void execute_generatesCodeWhenUserHadNone() {
        when(referralCodeRepository.findByUserId("user-3")).thenReturn(Optional.empty());
        when(codeGenerator.generateAndSave("user-3"))
                .thenReturn(ReferralCode.builder().id("rc-3").userId("user-3").code("FIRST1").build());

        ReferralCodeDTO result = service.execute("user-3");

        verify(referralCodeRepository, never()).save(any());
        assertThat(result.getCode()).isEqualTo("FIRST1");
    }
}

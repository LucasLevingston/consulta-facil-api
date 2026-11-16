package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.referral.ReferralCodeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateReferralCodeServiceTest {

    @Mock ReferralCodeRepositoryPort referralCodeRepository;
    @Mock ReferralCodeGenerator codeGenerator;

    GetOrCreateReferralCodeService service;

    @BeforeEach
    void setUp() {
        service = new GetOrCreateReferralCodeService(referralCodeRepository, codeGenerator, new ReferralCodeMapper());
    }

    @Test
    void execute_returnsExistingCodeWithoutGeneratingANewOne() {
        when(referralCodeRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(ReferralCode.builder().id("rc-1").userId("user-1").code("OLD123").build()));

        ReferralCodeDTO result = service.execute("user-1");

        assertThat(result.getCode()).isEqualTo("OLD123");
        assertThat(result.isActive()).isTrue();
        verifyNoInteractions(codeGenerator);
    }

    @Test
    void execute_generatesCodeWhenUserHasNone() {
        when(referralCodeRepository.findByUserId("user-2")).thenReturn(Optional.empty());
        when(codeGenerator.generateAndSave("user-2"))
                .thenReturn(ReferralCode.builder().id("rc-2").userId("user-2").code("NEW456").build());

        ReferralCodeDTO result = service.execute("user-2");

        verify(codeGenerator).generateAndSave("user-2");
        assertThat(result.getId()).isEqualTo("rc-2");
        assertThat(result.getCode()).isEqualTo("NEW456");
        assertThat(result.getUserId()).isEqualTo("user-2");
    }
}

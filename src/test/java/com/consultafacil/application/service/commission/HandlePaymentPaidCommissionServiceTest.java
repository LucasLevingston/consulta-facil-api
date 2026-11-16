package com.consultafacil.application.service.commission;

import com.consultafacil.application.port.in.commission.CreateCommissionUseCase;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlePaymentPaidCommissionServiceTest {

    @Mock ReferralRepositoryPort referralRepository;
    @Mock CreateCommissionUseCase createCommission;

    HandlePaymentPaidCommissionService service;

    @BeforeEach
    void setUp() {
        service = new HandlePaymentPaidCommissionService(referralRepository, createCommission);
    }

    @Test
    void execute_firstPaymentOfReferredUser_marksReferralAndCreatesCommission() {
        Referral referral = Referral.builder().id("ref-1").referrerId("referrer-1").referredId("payer-1").build();
        when(referralRepository.findByReferredId("payer-1")).thenReturn(Optional.of(referral));

        service.execute("pay-1", new BigDecimal("80.00"), "payer-1");

        assertThat(referral.getFirstPaymentId()).isEqualTo("pay-1");
        verify(referralRepository).save(referral);
        verify(createCommission).execute("ref-1", "pay-1", new BigDecimal("80.00"), "referrer-1");
    }

    @Test
    void execute_laterPayments_doNotGenerateNewCommissions() {
        Referral referral = Referral.builder().id("ref-1").referrerId("referrer-1").firstPaymentId("pay-0").build();
        when(referralRepository.findByReferredId("payer-1")).thenReturn(Optional.of(referral));

        service.execute("pay-2", new BigDecimal("80.00"), "payer-1");

        assertThat(referral.getFirstPaymentId()).isEqualTo("pay-0");
        verify(referralRepository, never()).save(any());
        verifyNoInteractions(createCommission);
    }

    @Test
    void execute_payerWasNotReferred_doesNothing() {
        when(referralRepository.findByReferredId("payer-9")).thenReturn(Optional.empty());

        service.execute("pay-1", BigDecimal.TEN, "payer-9");

        verify(referralRepository, never()).save(any());
        verifyNoInteractions(createCommission);
    }
}

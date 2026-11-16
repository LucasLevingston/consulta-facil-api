package com.consultafacil.application.service.commission;

import com.consultafacil.application.port.in.wallet.ReleasePendingWalletCommissionUseCase;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
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
class CancelCommissionServiceTest {

    @Mock ReferralCommissionRepositoryPort commissionRepository;
    @Mock ReferralRepositoryPort referralRepository;
    @Mock ReleasePendingWalletCommissionUseCase releasePendingWalletCommission;

    CancelCommissionService service;

    @BeforeEach
    void setUp() {
        service = new CancelCommissionService(commissionRepository, referralRepository, releasePendingWalletCommission);
    }

    private ReferralCommission commission(CommissionStatus status) {
        return ReferralCommission.builder()
                .id("c-1")
                .referralId("ref-1")
                .paymentId("pay-1")
                .amount(new BigDecimal("12.50"))
                .status(status)
                .build();
    }

    @Test
    void execute_pendingCommission_releasesPendingWalletBalanceAndCancels() {
        ReferralCommission commission = commission(CommissionStatus.PENDING);
        when(commissionRepository.findByPaymentId("pay-1")).thenReturn(Optional.of(commission));
        when(referralRepository.findById("ref-1"))
                .thenReturn(Optional.of(Referral.builder().id("ref-1").referrerId("referrer-1").build()));

        service.execute("pay-1");

        verify(releasePendingWalletCommission).execute("referrer-1", new BigDecimal("12.50"));
        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.CANCELED);
        verify(commissionRepository).save(commission);
    }

    @Test
    void execute_availableCommission_cancelsWithoutTouchingPendingWallet() {
        ReferralCommission commission = commission(CommissionStatus.AVAILABLE);
        when(commissionRepository.findByPaymentId("pay-1")).thenReturn(Optional.of(commission));

        service.execute("pay-1");

        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.CANCELED);
        verify(commissionRepository).save(commission);
        verifyNoInteractions(referralRepository, releasePendingWalletCommission);
    }

    @Test
    void execute_alreadyCanceled_isIdempotent() {
        when(commissionRepository.findByPaymentId("pay-1")).thenReturn(Optional.of(commission(CommissionStatus.CANCELED)));

        service.execute("pay-1");

        verify(commissionRepository, never()).save(any());
        verifyNoInteractions(releasePendingWalletCommission);
    }

    @Test
    void execute_noCommissionForPayment_doesNothing() {
        when(commissionRepository.findByPaymentId("pay-x")).thenReturn(Optional.empty());

        service.execute("pay-x");

        verify(commissionRepository, never()).save(any());
        verifyNoInteractions(referralRepository, releasePendingWalletCommission);
    }

    @Test
    void execute_pendingCommissionWithMissingReferral_stillCancels() {
        ReferralCommission commission = commission(CommissionStatus.PENDING);
        when(commissionRepository.findByPaymentId("pay-1")).thenReturn(Optional.of(commission));
        when(referralRepository.findById("ref-1")).thenReturn(Optional.empty());

        service.execute("pay-1");

        verifyNoInteractions(releasePendingWalletCommission);
        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.CANCELED);
        verify(commissionRepository).save(commission);
    }
}

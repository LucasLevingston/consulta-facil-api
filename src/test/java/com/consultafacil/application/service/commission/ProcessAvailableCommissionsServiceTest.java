package com.consultafacil.application.service.commission;

import com.consultafacil.application.port.in.wallet.CreditWalletFromCommissionUseCase;
import com.consultafacil.application.port.in.wallet.ReleasePendingWalletCommissionUseCase;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessAvailableCommissionsServiceTest {

    @Mock ReferralCommissionRepositoryPort commissionRepository;
    @Mock ReferralRepositoryPort referralRepository;
    @Mock BillingPaymentRepositoryPort billingPaymentRepository;
    @Mock CreditWalletFromCommissionUseCase creditWallet;
    @Mock ReleasePendingWalletCommissionUseCase releasePending;

    ProcessAvailableCommissionsService service;

    @BeforeEach
    void setUp() {
        service = new ProcessAvailableCommissionsService(commissionRepository, referralRepository,
                billingPaymentRepository, creditWallet, releasePending);
    }

    private ReferralCommission pending(String id, String paymentId) {
        return ReferralCommission.builder()
                .id(id)
                .referralId("ref-1")
                .paymentId(paymentId)
                .amount(new BigDecimal("15.00"))
                .build();
    }

    private void paymentWithStatus(String paymentId, BillingPaymentStatus status) {
        when(billingPaymentRepository.findById(paymentId))
                .thenReturn(Optional.of(BillingPayment.builder().id(paymentId).status(status).build()));
    }

    @Test
    void execute_makesMaturedCommissionAvailableAndMovesBalanceToAvailableWallet() {
        ReferralCommission commission = pending("c-1", "pay-1");
        when(commissionRepository.findByStatusAndAvailableAtBefore(eq(CommissionStatus.PENDING), any()))
                .thenReturn(List.of(commission));
        paymentWithStatus("pay-1", BillingPaymentStatus.PAID);
        when(referralRepository.findById("ref-1"))
                .thenReturn(Optional.of(Referral.builder().id("ref-1").referrerId("referrer-1").build()));

        service.execute();

        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.AVAILABLE);
        verify(commissionRepository).save(commission);
        verify(creditWallet).execute("referrer-1", new BigDecimal("15.00"), "c-1");
        verify(releasePending).execute("referrer-1", new BigDecimal("15.00"));
    }

    @ParameterizedTest
    @EnumSource(value = BillingPaymentStatus.class, names = {"REFUNDED", "CANCELED"})
    void execute_skipsCommissionsWhosePaymentWasReversed(BillingPaymentStatus status) {
        ReferralCommission commission = pending("c-1", "pay-1");
        when(commissionRepository.findByStatusAndAvailableAtBefore(eq(CommissionStatus.PENDING), any()))
                .thenReturn(List.of(commission));
        paymentWithStatus("pay-1", status);

        service.execute();

        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.PENDING);
        verify(commissionRepository, never()).save(any());
        verifyNoInteractions(creditWallet, releasePending);
    }

    @Test
    void execute_skipsCommissionsWhosePaymentNoLongerExists() {
        when(commissionRepository.findByStatusAndAvailableAtBefore(eq(CommissionStatus.PENDING), any()))
                .thenReturn(List.of(pending("c-1", "gone")));
        when(billingPaymentRepository.findById("gone")).thenReturn(Optional.empty());

        service.execute();

        verify(commissionRepository, never()).save(any());
        verifyNoInteractions(creditWallet, releasePending);
    }

    @Test
    void execute_processesValidCommissionsEvenWhenOthersAreSkipped() {
        ReferralCommission refunded = pending("c-1", "pay-1");
        ReferralCommission valid = pending("c-2", "pay-2");
        when(commissionRepository.findByStatusAndAvailableAtBefore(eq(CommissionStatus.PENDING), any()))
                .thenReturn(List.of(refunded, valid));
        paymentWithStatus("pay-1", BillingPaymentStatus.REFUNDED);
        paymentWithStatus("pay-2", BillingPaymentStatus.PAID);
        when(referralRepository.findById("ref-1"))
                .thenReturn(Optional.of(Referral.builder().id("ref-1").referrerId("referrer-1").build()));

        service.execute();

        assertThat(refunded.getStatus()).isEqualTo(CommissionStatus.PENDING);
        assertThat(valid.getStatus()).isEqualTo(CommissionStatus.AVAILABLE);
        verify(creditWallet).execute("referrer-1", new BigDecimal("15.00"), "c-2");
    }

    @Test
    void execute_marksAvailableEvenIfReferralWasDeleted() {
        ReferralCommission commission = pending("c-1", "pay-1");
        when(commissionRepository.findByStatusAndAvailableAtBefore(eq(CommissionStatus.PENDING), any()))
                .thenReturn(List.of(commission));
        paymentWithStatus("pay-1", BillingPaymentStatus.PAID);
        when(referralRepository.findById("ref-1")).thenReturn(Optional.empty());

        service.execute();

        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.AVAILABLE);
        verifyNoInteractions(creditWallet, releasePending);
    }

    @Test
    void execute_withNothingPending_doesNothing() {
        when(commissionRepository.findByStatusAndAvailableAtBefore(eq(CommissionStatus.PENDING), any()))
                .thenReturn(List.of());

        service.execute();

        verifyNoInteractions(billingPaymentRepository, referralRepository, creditWallet, releasePending);
    }
}

package com.consultafacil.application.service.commission;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.application.port.in.wallet.AddPendingWalletCommissionUseCase;
import com.consultafacil.application.service.referral.ReferralCommissionMapper;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCommissionServiceTest {

    @Mock ReferralCommissionRepositoryPort commissionRepository;
    @Mock BillingPaymentRepositoryPort billingPaymentRepository;
    @Mock AddPendingWalletCommissionUseCase addPendingWalletCommission;

    CreateCommissionService service;

    @BeforeEach
    void setUp() {
        service = new CreateCommissionService(commissionRepository, billingPaymentRepository,
                addPendingWalletCommission, new ReferralCommissionMapper());
    }

    private void saveAssignsId() {
        when(commissionRepository.save(any())).thenAnswer(inv -> {
            ReferralCommission c = inv.getArgument(0);
            c.setId("c-new");
            return c;
        });
    }

    @Test
    void execute_createsTenPercentCommissionAvailableThirtyDaysAfterPayment() {
        LocalDateTime paidAt = LocalDateTime.of(2026, 5, 1, 10, 0);
        when(commissionRepository.existsByReferralIdAndPaymentId("ref-1", "pay-1")).thenReturn(false);
        when(billingPaymentRepository.findById("pay-1"))
                .thenReturn(Optional.of(BillingPayment.builder().id("pay-1").paidAt(paidAt).build()));
        saveAssignsId();

        ReferralCommissionDTO result = service.execute("ref-1", "pay-1", new BigDecimal("199.90"), "referrer-1");

        ArgumentCaptor<ReferralCommission> saved = ArgumentCaptor.forClass(ReferralCommission.class);
        verify(commissionRepository).save(saved.capture());
        assertThat(saved.getValue().getAmount()).isEqualByComparingTo("19.99");
        assertThat(saved.getValue().getAvailableAt()).isEqualTo(paidAt.plusDays(30));
        assertThat(saved.getValue().getReferralId()).isEqualTo("ref-1");
        assertThat(saved.getValue().getPaymentId()).isEqualTo("pay-1");
        verify(addPendingWalletCommission).execute("referrer-1", new BigDecimal("19.99"), "c-new");
        assertThat(result.getId()).isEqualTo("c-new");
        assertThat(result.getAmount()).isEqualByComparingTo("19.99");
    }

    @Test
    void execute_usesNowWhenPaymentHasNoPaidAt() {
        when(commissionRepository.existsByReferralIdAndPaymentId("ref-1", "pay-1")).thenReturn(false);
        when(billingPaymentRepository.findById("pay-1"))
                .thenReturn(Optional.of(BillingPayment.builder().id("pay-1").build()));
        saveAssignsId();

        service.execute("ref-1", "pay-1", new BigDecimal("100.00"), "referrer-1");

        ArgumentCaptor<ReferralCommission> saved = ArgumentCaptor.forClass(ReferralCommission.class);
        verify(commissionRepository).save(saved.capture());
        assertThat(saved.getValue().getAvailableAt())
                .isCloseTo(LocalDateTime.now().plusDays(30), within(1, ChronoUnit.MINUTES));
    }

    @Test
    void execute_usesNowWhenPaymentIsUnknown() {
        when(commissionRepository.existsByReferralIdAndPaymentId("ref-1", "pay-1")).thenReturn(false);
        when(billingPaymentRepository.findById("pay-1")).thenReturn(Optional.empty());
        saveAssignsId();

        service.execute("ref-1", "pay-1", new BigDecimal("100.00"), "referrer-1");

        ArgumentCaptor<ReferralCommission> saved = ArgumentCaptor.forClass(ReferralCommission.class);
        verify(commissionRepository).save(saved.capture());
        assertThat(saved.getValue().getAmount()).isEqualByComparingTo("10.00");
        assertThat(saved.getValue().getAvailableAt())
                .isCloseTo(LocalDateTime.now().plusDays(30), within(1, ChronoUnit.MINUTES));
    }

    @Test
    void execute_returnsExistingCommissionWithoutDuplicating() {
        when(commissionRepository.existsByReferralIdAndPaymentId("ref-1", "pay-1")).thenReturn(true);
        when(commissionRepository.findByPaymentId("pay-1")).thenReturn(Optional.of(ReferralCommission.builder()
                .id("c-old").referralId("ref-1").paymentId("pay-1").amount(new BigDecimal("5.00")).build()));

        ReferralCommissionDTO result = service.execute("ref-1", "pay-1", new BigDecimal("50.00"), "referrer-1");

        assertThat(result.getId()).isEqualTo("c-old");
        verify(commissionRepository, never()).save(any());
        verifyNoInteractions(billingPaymentRepository, addPendingWalletCommission);
    }
}

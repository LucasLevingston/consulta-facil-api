package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;
import com.consultafacil.application.port.in.referral.GetOrCreateReferralCodeUseCase;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserReferralStatsServiceTest {

    @Mock GetOrCreateReferralCodeUseCase getOrCreateReferralCode;
    @Mock ReferralRepositoryPort referralRepository;
    @Mock ReferralCommissionRepositoryPort commissionRepository;
    @Mock WalletRepositoryPort walletRepository;

    GetUserReferralStatsService service;

    @BeforeEach
    void setUp() {
        service = new GetUserReferralStatsService(getOrCreateReferralCode, referralRepository,
                commissionRepository, walletRepository);
        when(getOrCreateReferralCode.execute("user-1")).thenReturn(ReferralCodeDTO.builder().code("ABC123").build());
    }

    private ReferralCommission commission(String referralId, CommissionStatus status) {
        return ReferralCommission.builder().referralId(referralId).amount(BigDecimal.ONE).status(status).build();
    }

    @Test
    void execute_countsOnlyCommissionsOfTheUsersOwnReferrals() {
        when(referralRepository.findAllByReferrerId("user-1")).thenReturn(List.of(
                Referral.builder().id("r1").build(),
                Referral.builder().id("r2").build()));
        when(commissionRepository.findAll()).thenReturn(List.of(
                commission("r1", CommissionStatus.PENDING),
                commission("r2", CommissionStatus.PENDING),
                commission("r2", CommissionStatus.AVAILABLE),
                commission("someone-else", CommissionStatus.PENDING),
                commission("r1", CommissionStatus.CANCELED)));
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(Wallet.builder()
                .userId("user-1")
                .balance(new BigDecimal("30.00"))
                .pendingBalance(new BigDecimal("12.00"))
                .build()));

        ReferralStatsDTO stats = service.execute("user-1");

        assertThat(stats.getCode()).isEqualTo("ABC123");
        assertThat(stats.getTotalReferred()).isEqualTo(2);
        assertThat(stats.getPendingCommissions()).isEqualTo(2);
        assertThat(stats.getAvailableCommissions()).isEqualTo(1);
        assertThat(stats.getAvailableBalance()).isEqualByComparingTo("30.00");
        assertThat(stats.getPendingBalance()).isEqualByComparingTo("12.00");
    }

    @Test
    void execute_userWithoutReferralsOrWallet_returnsZeros() {
        when(referralRepository.findAllByReferrerId("user-1")).thenReturn(List.of());
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        ReferralStatsDTO stats = service.execute("user-1");

        assertThat(stats.getTotalReferred()).isZero();
        assertThat(stats.getPendingCommissions()).isZero();
        assertThat(stats.getAvailableCommissions()).isZero();
        assertThat(stats.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verifyNoInteractions(commissionRepository);
    }
}

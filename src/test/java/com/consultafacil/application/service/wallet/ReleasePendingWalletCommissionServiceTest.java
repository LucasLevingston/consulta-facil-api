package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleasePendingWalletCommissionServiceTest {

    @Mock GetOrCreateWalletUseCase getOrCreateWallet;
    @Mock WalletRepositoryPort walletRepository;

    ReleasePendingWalletCommissionService service;

    @BeforeEach
    void setUp() {
        service = new ReleasePendingWalletCommissionService(getOrCreateWallet, walletRepository);
    }

    @Test
    void execute_reducesPendingBalanceAndKeepsAvailableBalance() {
        Wallet wallet = Wallet.builder().id("w-1").userId("user-1")
                .balance(new BigDecimal("7.00")).pendingBalance(new BigDecimal("10.00")).build();
        when(getOrCreateWallet.execute("user-1")).thenReturn(wallet);

        service.execute("user-1", new BigDecimal("4.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo("6.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("7.00");
        verify(walletRepository).save(wallet);
    }

    @Test
    void execute_clampsPendingBalanceAtZero() {
        Wallet wallet = Wallet.builder().id("w-1").userId("user-1").pendingBalance(new BigDecimal("1.00")).build();
        when(getOrCreateWallet.execute("user-1")).thenReturn(wallet);

        service.execute("user-1", new BigDecimal("4.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

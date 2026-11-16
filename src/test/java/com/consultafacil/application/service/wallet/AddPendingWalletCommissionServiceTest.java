package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.enums.WalletTransactionType;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import com.consultafacil.domain.port.out.wallet.WalletTransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddPendingWalletCommissionServiceTest {

    @Mock GetOrCreateWalletUseCase getOrCreateWallet;
    @Mock WalletRepositoryPort walletRepository;
    @Mock WalletTransactionRepositoryPort walletTransactionRepository;

    AddPendingWalletCommissionService service;

    @BeforeEach
    void setUp() {
        service = new AddPendingWalletCommissionService(getOrCreateWallet, walletRepository, walletTransactionRepository);
    }

    @Test
    void execute_increasesPendingBalanceAndRecordsPendingCommissionTransaction() {
        Wallet wallet = Wallet.builder().id("w-1").userId("user-1").pendingBalance(new BigDecimal("4.00")).build();
        when(getOrCreateWallet.execute("user-1")).thenReturn(wallet);

        service.execute("user-1", new BigDecimal("6.00"), "c-1");

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo("10.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("0");
        verify(walletRepository).save(wallet);
        ArgumentCaptor<WalletTransaction> tx = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(tx.capture());
        assertThat(tx.getValue().getWalletId()).isEqualTo("w-1");
        assertThat(tx.getValue().getType()).isEqualTo(WalletTransactionType.REFERRAL_COMMISSION);
        assertThat(tx.getValue().getAmount()).isEqualByComparingTo("6.00");
        assertThat(tx.getValue().getReferenceId()).isEqualTo("c-1");
        assertThat(tx.getValue().getReferenceType()).isEqualTo("PENDING_COMMISSION");
    }
}

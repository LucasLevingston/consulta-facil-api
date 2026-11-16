package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import com.consultafacil.core.exception.BadRequestException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitWalletServiceTest {

    @Mock GetOrCreateWalletUseCase getOrCreateWallet;
    @Mock WalletRepositoryPort walletRepository;
    @Mock WalletTransactionRepositoryPort walletTransactionRepository;

    DebitWalletService service;

    @BeforeEach
    void setUp() {
        service = new DebitWalletService(getOrCreateWallet, walletRepository, walletTransactionRepository);
    }

    @Test
    void execute_debitsBalanceAndRecordsWithdrawal() {
        Wallet wallet = Wallet.builder().id("w-1").userId("user-1").balance(new BigDecimal("50.00")).build();
        when(getOrCreateWallet.execute("user-1")).thenReturn(wallet);

        service.execute("user-1", new BigDecimal("20.00"), "Saque via PIX");

        assertThat(wallet.getBalance()).isEqualByComparingTo("30.00");
        verify(walletRepository).save(wallet);
        ArgumentCaptor<WalletTransaction> tx = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(tx.capture());
        assertThat(tx.getValue().getType()).isEqualTo(WalletTransactionType.WITHDRAW);
        assertThat(tx.getValue().getAmount()).isEqualByComparingTo("20.00");
        assertThat(tx.getValue().getDescription()).isEqualTo("Saque via PIX");
    }

    @Test
    void execute_insufficientBalance_throwsAndPersistsNothing() {
        Wallet wallet = Wallet.builder().id("w-1").userId("user-1").balance(new BigDecimal("5.00")).build();
        when(getOrCreateWallet.execute("user-1")).thenReturn(wallet);

        assertThatThrownBy(() -> service.execute("user-1", new BigDecimal("20.00"), "Saque"))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(walletRepository, walletTransactionRepository);
    }
}

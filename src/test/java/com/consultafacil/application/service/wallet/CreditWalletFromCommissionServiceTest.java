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
class CreditWalletFromCommissionServiceTest {

    @Mock GetOrCreateWalletUseCase getOrCreateWallet;
    @Mock WalletRepositoryPort walletRepository;
    @Mock WalletTransactionRepositoryPort walletTransactionRepository;

    CreditWalletFromCommissionService service;

    @BeforeEach
    void setUp() {
        service = new CreditWalletFromCommissionService(getOrCreateWallet, walletRepository, walletTransactionRepository);
    }

    @Test
    void execute_creditsAvailableBalanceAndRecordsReleasedCommission() {
        Wallet wallet = Wallet.builder().id("w-1").userId("user-1").balance(new BigDecimal("1.00")).build();
        when(getOrCreateWallet.execute("user-1")).thenReturn(wallet);

        service.execute("user-1", new BigDecimal("15.00"), "c-9");

        assertThat(wallet.getBalance()).isEqualByComparingTo("16.00");
        verify(walletRepository).save(wallet);
        ArgumentCaptor<WalletTransaction> tx = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(tx.capture());
        assertThat(tx.getValue().getType()).isEqualTo(WalletTransactionType.REFERRAL_COMMISSION);
        assertThat(tx.getValue().getAmount()).isEqualByComparingTo("15.00");
        assertThat(tx.getValue().getReferenceId()).isEqualTo("c-9");
        assertThat(tx.getValue().getReferenceType()).isEqualTo("REFERRAL_COMMISSION");
        assertThat(tx.getValue().getDescription()).isEqualTo("Comissão de indicação liberada");
    }
}

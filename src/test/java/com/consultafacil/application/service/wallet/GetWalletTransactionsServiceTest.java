package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.enums.WalletTransactionType;
import com.consultafacil.domain.port.out.wallet.WalletTransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWalletTransactionsServiceTest {

    @Mock GetOrCreateWalletUseCase getOrCreateWallet;
    @Mock WalletTransactionRepositoryPort walletTransactionRepository;

    GetWalletTransactionsService service;

    @BeforeEach
    void setUp() {
        service = new GetWalletTransactionsService(getOrCreateWallet, walletTransactionRepository, new WalletTransactionMapper());
    }

    @Test
    void execute_listsTransactionsOfTheUsersWalletInRepositoryOrder() {
        when(getOrCreateWallet.execute("user-1")).thenReturn(Wallet.builder().id("w-1").userId("user-1").build());
        when(walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc("w-1")).thenReturn(List.of(
                WalletTransaction.builder().id("t-2").walletId("w-1").type(WalletTransactionType.WITHDRAW)
                        .amount(BigDecimal.ONE).build(),
                WalletTransaction.builder().id("t-1").walletId("w-1").type(WalletTransactionType.REFERRAL_COMMISSION)
                        .amount(BigDecimal.TEN).build()));

        List<WalletTransactionDTO> result = service.execute("user-1");

        assertThat(result).extracting(WalletTransactionDTO::getId).containsExactly("t-2", "t-1");
        assertThat(result.get(0).getType()).isEqualTo(WalletTransactionType.WITHDRAW);
    }

    @Test
    void execute_newUserGetsEmptyHistory() {
        when(getOrCreateWallet.execute("new")).thenReturn(Wallet.builder().id("w-new").userId("new").build());
        when(walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc("w-new")).thenReturn(List.of());

        assertThat(service.execute("new")).isEmpty();
    }
}

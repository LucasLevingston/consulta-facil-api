package com.consultafacil.application.service.wallet;

import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateWalletServiceTest {

    @Mock WalletRepositoryPort walletRepository;

    GetOrCreateWalletService service;

    @BeforeEach
    void setUp() {
        service = new GetOrCreateWalletService(walletRepository);
    }

    @Test
    void execute_returnsExistingWallet() {
        Wallet existing = Wallet.builder().id("w-1").userId("user-1").balance(BigDecimal.TEN).build();
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(existing));

        assertThat(service.execute("user-1")).isSameAs(existing);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void execute_createsEmptyWalletWhenUserHasNone() {
        when(walletRepository.findByUserId("user-2")).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Wallet created = service.execute("user-2");

        ArgumentCaptor<Wallet> saved = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo("user-2");
        assertThat(created.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(created.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

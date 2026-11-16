package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWalletServiceTest {

    @Mock WalletRepositoryPort walletRepository;

    GetWalletService service;

    @BeforeEach
    void setUp() {
        service = new GetWalletService(walletRepository, new WalletMapper());
    }

    @Test
    void execute_returnsMappedWallet() {
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(Wallet.builder()
                .id("w-1").userId("user-1").balance(new BigDecimal("3.00")).pendingBalance(new BigDecimal("1.00")).build()));

        WalletDTO dto = service.execute("user-1");

        assertThat(dto.getId()).isEqualTo("w-1");
        assertThat(dto.getBalance()).isEqualByComparingTo("3.00");
        assertThat(dto.getPendingBalance()).isEqualByComparingTo("1.00");
    }

    @Test
    void execute_doesNotCreateMissingWallet() {
        when(walletRepository.findByUserId("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Wallet");
    }
}

package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllWalletsServiceTest {

    @Mock WalletRepositoryPort walletRepository;

    @Test
    void execute_mapsEveryWallet() {
        when(walletRepository.findAll()).thenReturn(List.of(
                Wallet.builder().id("w-1").userId("a").build(),
                Wallet.builder().id("w-2").userId("b").build()));

        List<WalletDTO> result = new GetAllWalletsService(walletRepository, new WalletMapper()).execute();

        assertThat(result).extracting(WalletDTO::getUserId).containsExactly("a", "b");
    }

    @Test
    void execute_returnsEmptyListWhenThereAreNoWallets() {
        when(walletRepository.findAll()).thenReturn(List.of());

        assertThat(new GetAllWalletsService(walletRepository, new WalletMapper()).execute()).isEmpty();
    }
}

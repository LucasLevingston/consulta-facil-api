package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateWalletServiceTest {

    @Mock GetOrCreateWalletUseCase getOrCreateWallet;

    @Test
    void execute_delegatesToGetOrCreateSoItIsIdempotent() {
        new CreateWalletService(getOrCreateWallet).execute("user-1");

        verify(getOrCreateWallet).execute("user-1");
    }
}

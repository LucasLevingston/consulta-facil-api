package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.CreateWalletUseCase;
import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWalletService implements CreateWalletUseCase {

    private final GetOrCreateWalletUseCase getOrCreateWalletUseCase;

    @Override
    @Transactional
    public void execute(String userId) {
        getOrCreateWalletUseCase.execute(userId);
    }
}

package com.consultafacil.application.port.in;

import com.consultafacil.domain.entity.Wallet;

public interface GetOrCreateWalletUseCase {
    Wallet execute(String userId);
}

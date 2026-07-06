package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;

public interface GetWalletUseCase {
    WalletDTO execute(String userId);
}

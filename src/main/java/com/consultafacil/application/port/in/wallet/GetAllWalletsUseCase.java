package com.consultafacil.application.port.in.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;

import java.util.List;

public interface GetAllWalletsUseCase {
    List<WalletDTO> execute();
}

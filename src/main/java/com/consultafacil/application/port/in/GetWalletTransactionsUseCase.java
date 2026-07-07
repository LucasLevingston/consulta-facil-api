package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;

import java.util.List;

public interface GetWalletTransactionsUseCase {
    List<WalletTransactionDTO> execute(String userId);
}

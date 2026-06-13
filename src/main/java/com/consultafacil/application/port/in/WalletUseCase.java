package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.domain.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletUseCase {
    WalletDTO getWallet(String userId);
    List<WalletTransactionDTO> getTransactions(String userId);
    void debit(String userId, BigDecimal amount, String description);
    List<WalletDTO> getAllWallets();
    Wallet getOrCreateWallet(String userId);
    void createWallet(String userId);
    void creditFromCommission(String userId, BigDecimal amount, String commissionId);
    void addPendingCommission(String userId, BigDecimal amount, String commissionId);
    void releasePending(String userId, BigDecimal amount);
}

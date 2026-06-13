package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.WalletTransaction;

import java.util.List;

public interface WalletTransactionRepositoryPort {
    WalletTransaction save(WalletTransaction transaction);
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);
}

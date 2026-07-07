package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.port.out.WalletTransactionRepositoryPort;
import com.consultafacil.domain.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepositoryPort {

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public WalletTransaction save(WalletTransaction transaction) {
        return walletTransactionRepository.save(transaction);
    }

    @Override
    public List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId) {
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }
}

package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);
}

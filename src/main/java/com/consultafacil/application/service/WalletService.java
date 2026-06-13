package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.application.port.in.WalletUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.enums.WalletTransactionType;
import com.consultafacil.domain.port.out.WalletRepositoryPort;
import com.consultafacil.domain.port.out.WalletTransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService implements WalletUseCase {

    private final WalletRepositoryPort walletRepository;
    private final WalletTransactionRepositoryPort walletTransactionRepository;

    @Override
    @Transactional
    public Wallet getOrCreateWallet(String userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet wallet = Wallet.builder().userId(userId).build();
            return walletRepository.save(wallet);
        });
    }

    @Override
    @Transactional
    public void createWallet(String userId) {
        getOrCreateWallet(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletDTO getWallet(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));
        return toDTO(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDTO> getTransactions(String userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void debit(String userId, BigDecimal amount, String description) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.debit(amount);
        walletRepository.save(wallet);
        walletTransactionRepository.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(WalletTransactionType.WITHDRAW)
                .amount(amount)
                .description(description)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletDTO> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void creditFromCommission(String userId, BigDecimal amount, String commissionId) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.credit(amount);
        walletRepository.save(wallet);
        walletTransactionRepository.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(WalletTransactionType.REFERRAL_COMMISSION)
                .amount(amount)
                .description("Comissão de indicação liberada")
                .referenceId(commissionId)
                .referenceType("REFERRAL_COMMISSION")
                .build());
    }

    @Override
    @Transactional
    public void addPendingCommission(String userId, BigDecimal amount, String commissionId) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.addPending(amount);
        walletRepository.save(wallet);
        walletTransactionRepository.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(WalletTransactionType.REFERRAL_COMMISSION)
                .amount(amount)
                .description("Comissão pendente de indicação")
                .referenceId(commissionId)
                .referenceType("PENDING_COMMISSION")
                .build());
    }

    @Override
    @Transactional
    public void releasePending(String userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.releasePending(amount);
        walletRepository.save(wallet);
    }

    private WalletDTO toDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .pendingBalance(wallet.getPendingBalance())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletTransactionDTO toTransactionDTO(WalletTransaction t) {
        return WalletTransactionDTO.builder()
                .id(t.getId())
                .walletId(t.getWalletId())
                .type(t.getType())
                .amount(t.getAmount())
                .description(t.getDescription())
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

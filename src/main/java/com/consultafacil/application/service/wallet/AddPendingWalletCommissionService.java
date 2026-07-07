package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.AddPendingWalletCommissionUseCase;
import com.consultafacil.application.port.in.GetOrCreateWalletUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.enums.WalletTransactionType;
import com.consultafacil.domain.port.out.WalletRepositoryPort;
import com.consultafacil.domain.port.out.WalletTransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AddPendingWalletCommissionService implements AddPendingWalletCommissionUseCase {

    private final GetOrCreateWalletUseCase getOrCreateWalletUseCase;
    private final WalletRepositoryPort walletRepository;
    private final WalletTransactionRepositoryPort walletTransactionRepository;

    @Override
    @Transactional
    public void execute(String userId, BigDecimal amount, String commissionId) {
        Wallet wallet = getOrCreateWalletUseCase.execute(userId);
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
}
